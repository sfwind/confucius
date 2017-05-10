package com.iquanwai.confucius.biz.domain.weixin.message;

/**
 * Created by justin on 17/5/10.
 */
public interface TextMessageService {
    boolean sendMessage(TextMessage textMessage);

    String SEND_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/mass/send?access_token={access_token}";
}
