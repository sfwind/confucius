package com.iquanwai.confucius.biz.domain.weixin.message;

import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.exception.MessageException;
import com.iquanwai.confucius.biz.exception.NotFollowingException;
import com.iquanwai.confucius.biz.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

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
            return handleText(document);
        //处理事件消息
        } else if (messageType.equals(TYPE_EVENT)) {
            return handleEvent(document);
        }
        return null;
    }

    private String handleText(Document document) {
        String openid = XMLHelper.getNode(document, FROM_USER);
        String toUser = XMLHelper.getNode(document, TO_USER);
        String content = XMLHelper.getNode(document, CONTENT);
        String replyMessage = messageReply(content, openid);
        return bulidReplyMessage(openid, toUser, replyMessage);
    }

    private String handleEvent(Document document) {
        String openid = XMLHelper.getNode(document, FROM_USER);
        String toUser = XMLHelper.getNode(document, TO_USER);
        String event = XMLHelper.getNode(document, EVENT);
        String eventKey = XMLHelper.getNode(document, EVENT_KEY);
        String replyMessage = eventReply(event, eventKey, openid);
        return bulidReplyMessage(openid, toUser, replyMessage);
    }

    private String bulidReplyMessage(String openid, String toUser, String replyMessage) {
        if (replyMessage != null) {
            TextMessage textMessage = new TextMessage();
            textMessage.content = XMLHelper.appendCDATA(replyMessage);
            textMessage.createTime = System.currentTimeMillis() / 1000;
            textMessage.fromUserName = XMLHelper.appendCDATA(toUser);
            textMessage.toUserName = XMLHelper.appendCDATA(openid);
            return XMLHelper.createXML(textMessage);
        }
        return null;
    }

    private String messageReply(String message, String openid) {

        // TODO:
        return message;
    }

    private String eventReply(String event, String eventKey, String openid) {
        if (event.equals(EVENT_SUBSCRIBE)) {
            if (eventKey != null) {
                logger.info("eventkey is {}", eventKey);
            }
            try {
                //更新用户信息
                accountService.getAccount(openid, true);
            } catch (NotFollowingException e) {
                // ignore
            }
            return "你好,欢迎关注";
        } else if (event.equals(EVENT_UNSUBSCRIBE)) {
            accountService.unfollow(openid);
        } else if (event.equals(EVENT_SCAN)){
            return "你好,欢迎扫码";
        }

        return null;
    }

}
