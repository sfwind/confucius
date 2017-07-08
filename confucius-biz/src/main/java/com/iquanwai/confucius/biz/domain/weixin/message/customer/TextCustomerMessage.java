package com.iquanwai.confucius.biz.domain.weixin.message.customer;

import lombok.Data;

/**
 * Created by justin on 17/7/8.
 */
@Data
public class TextCustomerMessage {
    private String touser;
    private String msgtype = "voice";
    private Text text;

    public TextCustomerMessage(String openid, String content){
        this.touser = openid;
        this.text = new Text(content);
    }
}
