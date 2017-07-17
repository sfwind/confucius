package com.iquanwai.confucius.biz.domain.weixin.message.callback;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.common.customer.PromotionUserDao;
import com.iquanwai.confucius.biz.dao.wx.AutoReplyMessageDao;
import com.iquanwai.confucius.biz.dao.wx.GraphicMessageDao;
import com.iquanwai.confucius.biz.dao.wx.SubscribeMessageDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.customer.CustomerMessageService;
import com.iquanwai.confucius.biz.exception.NotFollowingException;
import com.iquanwai.confucius.biz.po.AutoReplyMessage;
import com.iquanwai.confucius.biz.po.GraphicMessage;
import com.iquanwai.confucius.biz.po.PromotionUser;
import com.iquanwai.confucius.biz.po.SubscribeMessage;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.XMLHelper;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQPublisher;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/7/6.
 */
@Service
public class CallbackMessageServiceImpl implements CallbackMessageService {
    private static final String MESSAGE_TYPE = "MsgType";
    private static final String FROM_USER = "FromUserName";
    private static final String TO_USER = "ToUserName";
    private static final String CONTENT = "Content";
    private static final String EVENT = "Event";
    private static final String EVENT_KEY = "EventKey";
    @Autowired
    private AccountService accountService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private SubscribeMessageDao subscribeMessageDao;
    @Autowired
    private AutoReplyMessageDao autoReplyMessageDao;
    @Autowired
    private PromotionUserDao promotionUserDao;
    @Autowired
    private GraphicMessageDao graphicMessageDao;

    private RabbitMQPublisher rabbitMQPublisher;

    private Logger logger = LoggerFactory.getLogger(getClass());

    //关注事件
    private static final String EVENT_SUBSCRIBE = "subscribe";
    //取关事件
    private static final String EVENT_UNSUBSCRIBE = "unsubscribe";
    //扫码关注事件
    private static final String EVENT_SCAN = "SCAN";

    private static final String TYPE_TEXT = "text";
    private static final String TYPE_EVENT = "event";

    private Map<String, AutoReplyMessage> autoReplyMessageMap = Maps.newHashMap();
    private AutoReplyMessage defaultReply;
    private Map<String, List<GraphicMessage>> newsMessageMap = Maps.newHashMap();

    @PostConstruct
    public void init() {
        autoReplyMessageMap.clear();
        newsMessageMap.clear();
        defaultReply = null;
        //初始化自动回复消息
        List<AutoReplyMessage> messages = autoReplyMessageDao.loadAllMessages();
        messages.forEach(autoReplyMessage -> {
            // 判断是否是默认回复
            if (autoReplyMessage.getIsDefault()) {
                defaultReply = autoReplyMessage;
                return;
            }

            String keyword = autoReplyMessage.getKeyword();
            if (keyword.contains("|")) {
                String[] word = keyword.split("\\|");
                for (String w : word) {
                    autoReplyMessageMap.put(w, autoReplyMessage);
                }
            } else {
                autoReplyMessageMap.put(autoReplyMessage.getKeyword(), autoReplyMessage);
            }
            //构造图文消息
            if (autoReplyMessage.getType().equals(Constants.WEIXIN_MESSAGE_TYPE.NEWS)) {
                //message字段存储图文消息的id,多条图文时,用|隔开
                String message = autoReplyMessage.getMessage();
                String[] word = message.split("\\|");
                List<GraphicMessage> graphicMessages = Lists.newArrayList();
                for (String w : word) {
                    GraphicMessage graphicMessage = graphicMessageDao.load(GraphicMessage.class, Integer.valueOf(w));
                    if (graphicMessage != null) {
                        graphicMessages.add(graphicMessage);
                    }
                }
                newsMessageMap.put(keyword, graphicMessages);
            }

        });
        logger.info("load auto reply message complete");
        //初始化mq
        rabbitMQPublisher = new RabbitMQPublisher();
        rabbitMQPublisher.init(SUBSCRIBE_TOPIC, ConfigUtils.getRabbitMQIp(),
                ConfigUtils.getRabbitMQPort());
    }

    @Override
    public String handleCallback(Document document) {
        String messageType = XMLHelper.getNode(document, MESSAGE_TYPE);
        //处理文字消息
        if (messageType.equals(TYPE_TEXT)) {
            return handleUserMessage(document);
            //处理事件消息
        } else if (messageType.equals(TYPE_EVENT)) {
            return handleEvent(document);
        }
        return null;
    }

    @Override
    public void reload() {
        init();
    }

    private String handleUserMessage(Document document) {
        String openid = XMLHelper.getNode(document, FROM_USER);
        String toUser = XMLHelper.getNode(document, TO_USER);
        String content = XMLHelper.getNode(document, CONTENT);
        return messageReply(content, openid, toUser);
    }

    private String handleEvent(Document document) {
        String openid = XMLHelper.getNode(document, FROM_USER);
        String toUser = XMLHelper.getNode(document, TO_USER);
        String event = XMLHelper.getNode(document, EVENT);
        String eventKey = XMLHelper.getNode(document, EVENT_KEY);
        return eventReply(event, eventKey, openid, toUser);
    }

    private String bulidTextReplyMessage(String openid, String wxid, String replyMessage) {
        if (replyMessage != null) {
            TextMessage message = new TextMessage(wxid, openid, replyMessage);
            return XMLHelper.createXML(message);
        }
        return null;
    }

    private String buildImageReplyMessage(String openid, String wxid, String replyMessage) {
        if (replyMessage != null) {
            ImageMessage message = new ImageMessage(wxid, openid, replyMessage);
            return XMLHelper.createXML(message);
        }
        return null;
    }

    private String buildVoiceReplyMessage(String openid, String wxid, String replyMessage) {
        if (replyMessage != null) {
            VoiceMessage message = new VoiceMessage(wxid, openid, replyMessage);
            return XMLHelper.createXML(message);
        }
        return null;
    }

    private String buildNewsReplyMessage(String openid, String wxid, String keyword) {
        if (keyword != null) {
            List<GraphicMessage> graphicMessages = newsMessageMap.get(keyword);
            NewsMessage newsMessage = new NewsMessage(wxid, openid, graphicMessages);
            return XMLHelper.createXML(newsMessage);
        }
        return null;
    }

    private String messageReply(String message, String openid, String wxid) {
        if (StringUtils.isEmpty(message)) {
            return null;
        }

        // 精确匹配
        AutoReplyMessage autoReplyMessage = autoReplyMessageMap.get(message);
        if (autoReplyMessage != null) {
            String msg = buildMessage(openid, wxid, autoReplyMessage);
            if (msg != null) {
                return msg;
            }
        }

        // 模糊匹配
        List<String> words = CommonUtils.separateWords(message);
        for (String word : words) {
            if (autoReplyMessageMap.get(word) != null) {
                autoReplyMessage = autoReplyMessageMap.get(word);
                if (!autoReplyMessage.getExact()) {
                    String msg = buildMessage(openid, wxid, autoReplyMessage);
                    if (msg != null) {
                        return msg;
                    }
                }
            }
        }

        //没有匹配到任何消息时,回复默认消息
        return buildMessage(openid, wxid, defaultReply);
    }

    private String buildMessage(String openid, String wxid, AutoReplyMessage autoReplyMessage) {
        if (autoReplyMessage == null) {
            return null;
        }
        if (Constants.WEIXIN_MESSAGE_TYPE.TEXT == autoReplyMessage.getType()) {
            return bulidTextReplyMessage(openid, wxid, autoReplyMessage.getMessage());
        } else if (Constants.WEIXIN_MESSAGE_TYPE.IMAGE == autoReplyMessage.getType()) {
            return buildImageReplyMessage(openid, wxid, autoReplyMessage.getMessage());
        } else if (Constants.WEIXIN_MESSAGE_TYPE.VOICE == autoReplyMessage.getType()) {
            return buildVoiceReplyMessage(openid, wxid, autoReplyMessage.getMessage());
        } else if (Constants.WEIXIN_MESSAGE_TYPE.NEWS == autoReplyMessage.getType()) {
            return buildNewsReplyMessage(openid, wxid, autoReplyMessage.getKeyword());
        }
        return null;
    }

    private String eventReply(String event, String eventKey, String openid, String wxid) {
        switch (event) {
            case EVENT_SUBSCRIBE:
                List<SubscribeMessage> subscribeMessages;
                if (StringUtils.isNotEmpty(eventKey)) {
                    logger.info("event key is {}", eventKey);
                    // 去掉前缀 qrscene_
                    String channel = eventKey.substring(8);
                    //TODO: 老用户判断
                    //发送订阅消息
                    SubscribeEvent subscribeEvent = new SubscribeEvent();
                    subscribeEvent.setOpenid(openid);
                    subscribeEvent.setScene(channel);
                    try {
                        rabbitMQPublisher.publish(subscribeEvent);
                    } catch (ConnectException e) {
                        logger.error("rabbit mq init failed");
                    }
                    // 插入推广数据
                    if (promotionUserDao.loadPromotion(openid) == null) {
                        PromotionUser promotionUser = new PromotionUser();
                        promotionUser.setSource(channel);
                        promotionUser.setOpenid(openid);
                        promotionUser.setAction(0);
                        promotionUserDao.insert(promotionUser);
                    }
                    subscribeMessages = subscribeMessageDao.loadSubscribeMessages();
                    subscribeMessages.addAll(subscribeMessageDao.loadSubscribeMessages(channel));
                } else {
                    subscribeMessages = subscribeMessageDao.loadSubscribeMessages();
                }
                try {
                    //更新用户信息
                    accountService.getAccount(openid, true);
                } catch (NotFollowingException e) {
                    // ignore
                }

                if (CollectionUtils.isNotEmpty(subscribeMessages)) {
                    return sendSubscribeMessage(openid, wxid, subscribeMessages);
                }
                break;
            case EVENT_UNSUBSCRIBE:
                accountService.unfollow(openid);
                break;
            case EVENT_SCAN:
                List<SubscribeMessage> scanMessages;
                if (StringUtils.isNotEmpty(eventKey)) {
                    logger.info("event key is {}", eventKey);
                    scanMessages = subscribeMessageDao.loadScanMessages();
                    scanMessages.addAll(subscribeMessageDao.loadScanMessages(eventKey));
                } else {
                    scanMessages = subscribeMessageDao.loadScanMessages();
                }
                if (CollectionUtils.isNotEmpty(scanMessages)) {
                    return sendSubscribeMessage(openid, wxid, scanMessages);
                }
                break;
        }

        return null;
    }

    private String sendSubscribeMessage(String openid, String wxid, List<SubscribeMessage> subscribeMessages) {
        SubscribeMessage lastOne = subscribeMessages.remove(subscribeMessages.size() - 1);
        logger.info("发送关注消息给{}", openid);
        //发送客服消息
        subscribeMessages.forEach(subscribeMessage -> customerMessageService.sendCustomerMessage(openid,
                subscribeMessage.getMessage(), subscribeMessage.getType()));
        if (Constants.WEIXIN_MESSAGE_TYPE.TEXT == lastOne.getType()) {
            return bulidTextReplyMessage(openid, wxid, lastOne.getMessage());
        } else if (Constants.WEIXIN_MESSAGE_TYPE.IMAGE == lastOne.getType()) {
            return buildImageReplyMessage(openid, wxid, lastOne.getMessage());
        } else if (Constants.WEIXIN_MESSAGE_TYPE.VOICE == lastOne.getType()) {
            return buildVoiceReplyMessage(openid, wxid, lastOne.getMessage());
        }

        return null;
    }

}
