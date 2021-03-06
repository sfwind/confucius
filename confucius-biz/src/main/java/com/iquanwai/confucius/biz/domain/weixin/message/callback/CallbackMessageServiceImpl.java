package com.iquanwai.confucius.biz.domain.weixin.message.callback;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.common.customer.PromotionUserDao;
import com.iquanwai.confucius.biz.dao.wx.AutoReplyMessageDao;
import com.iquanwai.confucius.biz.dao.wx.GraphicMessageDao;
import com.iquanwai.confucius.biz.dao.wx.SubscribeMessageDao;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.accesstoken.AccessTokenService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.customer.CustomerMessageService;
import com.iquanwai.confucius.biz.po.AutoReplyMessage;
import com.iquanwai.confucius.biz.po.GraphicMessage;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.PromotionUser;
import com.iquanwai.confucius.biz.po.SubscribeMessage;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.message.WechatMessage;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.XMLHelper;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
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
    private static final int FRESH = 0;
    private static final int OLD = 1;

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
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    /**
     * 发送订阅消息
     **/
    private RabbitMQPublisher rabbitMQPublisher;
    /**
     * 不同的关键字分发给不同的队列
     */
    private Map<String, RabbitMQPublisher> messageMQGroup = Maps.newHashMap();

    //推广活动分隔符,分割活动和用户id
    private static final String ACTIVITY_SEPERATE_CHAR = "_";

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
            // 分发消息初始化
            if (autoReplyMessage.getType().equals(Constants.WEIXIN_MESSAGE_TYPE.DISTRIBUTE)) {
                // 消息分发
                String[] word = autoReplyMessage.getKeyword().split("\\|");
                String topic = autoReplyMessage.getMessage();
                RabbitMQPublisher tempPublisher = rabbitMQFactory.initFanoutPublisher(topic);
                for (String w : word) {
                    messageMQGroup.put(w, tempPublisher);
                }
                return;
            }

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
        rabbitMQPublisher = rabbitMQFactory.initFanoutPublisher(SUBSCRIBE_TOPIC);
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

        // 没有匹配到，发送mq
        RabbitMQPublisher messageMqPublisher = messageMQGroup.get(message);

        if (messageMqPublisher != null) {
            WechatMessage wechatMessage = new WechatMessage();
            wechatMessage.setMessage(message);
            wechatMessage.setOpenid(openid);
            wechatMessage.setWxid(wxid);
            try {
                messageMqPublisher.publish(wechatMessage);
            } catch (ConnectException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            return null;
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
            // 关注事件
            case EVENT_SUBSCRIBE:
                //更新用户信息
                accountService.storeWeiXinUserInfoByMobileApp(openid);
                operationLogService.trace(() -> {
                    Profile profile = accountService.getProfile(openid);
                    return profile.getId();
                }, "wechatSubscribe", () -> {
                    OperationLogService.Prop prop = OperationLogService.props();
                    prop.add("subscribeChannel", eventKey.substring(8));
                    return prop;
                });
                List<SubscribeMessage> subscribeMessages;
                if (StringUtils.isNotEmpty(eventKey)) {
                    logger.info("event key is {}", eventKey);
                    // 去掉前缀 qrscene_
                    String channel = eventKey.substring(8);
                    //加锁防止微信消息重放
                    if (!redisUtil.tryLock(EVENT_SUBSCRIBE + ":" + openid, 1, 5)) {
                        return null;
                    }
                    //发送订阅消息
                    SubscribeEvent subscribeEvent = new SubscribeEvent();
                    subscribeEvent.setScene(channel);
                    subscribeEvent.setOpenid(openid);
                    subscribeEvent.setEvent(event);
                    try {
                        rabbitMQPublisher.publish(subscribeEvent);
                    } catch (ConnectException e) {
                        logger.error("rabbit mq init failed");
                    }
                    promotionSuccess(channel, FRESH, openid);
                    subscribeMessages = subscribeMessageDao.loadSubscribeMessages(channel);
                } else {
                    subscribeMessages = subscribeMessageDao.loadSubscribeMessages();
                }

                OperationLog operationLog = OperationLog.create().openid(openid)
                        .module("圈外同学")
                        .function("关注").action("扫码关注").memo(eventKey);
                operationLogService.log(operationLog);
                if (CollectionUtils.isNotEmpty(subscribeMessages)) {
                    return sendSubscribeMessage(openid, wxid, subscribeMessages);
                }
                break;
            // 取消关注事件
            case EVENT_UNSUBSCRIBE:
                accountService.unfollow(openid);
                operationLogService.trace(() -> {
                    Profile profile = accountService.getProfile(openid);
                    return profile.getId();
                }, "wechatUnSubscribe");
                break;
            // 扫描事件
            case EVENT_SCAN:
                //加锁防止微信消息重放
                if (!redisUtil.tryLock(EVENT_SUBSCRIBE + ":" + openid, 1, 5)) {
                    return null;
                }
                //发送订阅消息
                SubscribeEvent subscribeEvent = new SubscribeEvent();
                subscribeEvent.setScene(eventKey);
                subscribeEvent.setOpenid(openid);
                subscribeEvent.setEvent(event);
                try {
                    rabbitMQPublisher.publish(subscribeEvent);
                } catch (ConnectException e) {
                    logger.error("rabbit mq init failed");
                }
                promotionSuccess(eventKey, OLD, openid);
                List<SubscribeMessage> scanMessages = Lists.newArrayList();
                if (StringUtils.isNotEmpty(eventKey)) {
                    logger.info("event key is {}", eventKey);
                    scanMessages = subscribeMessageDao.loadScanMessages(eventKey);
                }
                if (CollectionUtils.isNotEmpty(scanMessages)) {
                    return sendSubscribeMessage(openid, wxid, scanMessages);
                }
                break;
            default:
                break;
        }

        return null;
    }

    private void promotionSuccess(String eventKey, Integer action, String openid) {
        // 插入推广数据
        if (promotionUserDao.loadPromotion(openid, eventKey) == null) {
            PromotionUser promotionUser = new PromotionUser();
            promotionUser.setSource(eventKey);
            promotionUser.setOpenid(openid);
            promotionUser.setAction(action);
            if (eventKey.contains(ACTIVITY_SEPERATE_CHAR)) {
                String[] splits = StringUtils.split(eventKey, ACTIVITY_SEPERATE_CHAR);
                if (splits.length > 1) {
                    try {
                        int profileId = Integer.valueOf(splits[1]);
                        Profile profile = accountService.getProfile(profileId);
                        if (profile != null) {
                            if (profile.getOpenid().equals(openid)) {
                                return;
                            }
                        }
                        promotionUser.setProfileId(profileId);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
            promotionUserDao.insert(promotionUser);
        }
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
