package com.iquanwai.confucius.biz.domain.weixin.message;

import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.XMLHelper;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

/**
 * Created by justin on 17/7/6.
 */
@Service
public class CallbackMessageServiceImpl implements CallbackMessageService {
    private static final String SUCCESS = "success";

    private static final String MESSAGE_TYPE = "MsgType";
    private static final String FROM_USER = "FromUserName";
    private static final String CONTENT = "Content";

    private static final String TEXT = "text";
    @Override
    public String handleCallback(Document document) {
        String messageType = XMLHelper.getNode(document, MESSAGE_TYPE);
        if(messageType.equals(TEXT)){
            return handleText(document);
        }
        return SUCCESS;
    }

    private String handleText(Document document){
        String openid = XMLHelper.getNode(document, FROM_USER);
        String content = XMLHelper.getNode(document, CONTENT);
        TextMessage textMessage = new TextMessage();
        textMessage.content = content;
        textMessage.createTime = System.currentTimeMillis()/1000;
        textMessage.fromUserName = ConfigUtils.getAppid();
        textMessage.toUserName = openid;
        String xml = XMLHelper.createXML(textMessage);
        return xml;
    }

}
