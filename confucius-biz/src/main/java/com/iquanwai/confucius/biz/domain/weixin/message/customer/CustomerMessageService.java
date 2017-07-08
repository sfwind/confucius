package com.iquanwai.confucius.biz.domain.weixin.message.customer;

/**
 * Created by justin on 17/7/8.
 */
public interface CustomerMessageService {
    void sendCustomerMessage(String openid, String message, Integer type);

    Integer TEXT = 1;
    Integer IMAGE = 2;
    Integer VOICE = 3;

    String SEND_CUSTOMER_MESSAGE_URL ="https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token={access_token}";

}
