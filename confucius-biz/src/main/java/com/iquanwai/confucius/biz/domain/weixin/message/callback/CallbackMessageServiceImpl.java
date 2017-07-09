package com.iquanwai.confucius.biz.domain.weixin.message.callback;

import com.iquanwai.confucius.biz.dao.wx.SubscribeMessageDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.customer.CustomerMessageService;
import com.iquanwai.confucius.biz.exception.NotFollowingException;
import com.iquanwai.confucius.biz.po.SubscribeMessage;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.XMLHelper;
import com.qiniu.util.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.util.List;

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

    private Logger logger = LoggerFactory.getLogger(getClass());

    //关注事件
    private static final String EVENT_SUBSCRIBE = "subscribe";
    //取关事件
    private static final String EVENT_UNSUBSCRIBE = "unsubscribe";
    //扫码关注事件
    private static final String EVENT_SCAN = "SCAN";

    private static final String TYPE_TEXT = "text";
    private static final String TYPE_EVENT = "event";

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

    private String messageReply(String message, String openid, String wxid) {
        List<String> words = CommonUtils.separateWords(message);
        String reply = null;
        if(CollectionUtils.isNotEmpty(words)){
            reply = "有效词语:"+StringUtils.join(words, ",");
        }
        return bulidTextReplyMessage(openid, wxid, reply);
    }

    private String eventReply(String event, String eventKey, String openid, String wxid) {
        switch (event) {
            case EVENT_SUBSCRIBE:
                List<SubscribeMessage> subscribeMessages;
                if (eventKey != null) {
                    logger.info("eventkey is {}", eventKey);
                    // 去掉前缀 qrscene_
                    String channel = eventKey.substring(8);
                    subscribeMessages = subscribeMessageDao.loadSubscribeMessages(channel);
                }else{
                    subscribeMessages = subscribeMessageDao.loadSubscribeMessages();
                }
                try {
                    //更新用户信息
                    accountService.getAccount(openid, true);
                } catch (NotFollowingException e) {
                    // ignore
                }

                if(CollectionUtils.isNotEmpty(subscribeMessages)){
                    return sendSubscribeMessage(openid, wxid, subscribeMessages);
                }
                break;
            case EVENT_UNSUBSCRIBE:
                accountService.unfollow(openid);
                break;
            case EVENT_SCAN:
                List<SubscribeMessage> scanMessages = subscribeMessageDao.loadScanMessages();
                if(CollectionUtils.isNotEmpty(scanMessages)){
                    return sendSubscribeMessage(openid, wxid, scanMessages);
                }
                break;
        }

        return null;
    }

    private String sendSubscribeMessage(String openid, String wxid, List<SubscribeMessage> subscribeMessages) {
        SubscribeMessage lastOne = subscribeMessages.remove(subscribeMessages.size() - 1);
        //发送客服消息
        subscribeMessages.forEach(subscribeMessage -> {
            customerMessageService.sendCustomerMessage(openid,
                    subscribeMessage.getMessage(), subscribeMessage.getType());
        });
        if (CustomerMessageService.TEXT.equals(lastOne.getType())) {
            return bulidTextReplyMessage(openid, wxid, lastOne.getMessage());
        } else if(CustomerMessageService.IMAGE.equals(lastOne.getType())){
            return buildImageReplyMessage(openid, wxid, lastOne.getMessage());
        } else if(CustomerMessageService.VOICE.equals(lastOne.getType())){
            return buildVoiceReplyMessage(openid, wxid, lastOne.getMessage());
        }

        return null;
    }

}
