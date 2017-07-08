package com.iquanwai.confucius.biz.domain.weixin.message.customer;

import lombok.Data;

/**
 * Created by justin on 17/7/8.
 */
@Data
public class VoiceCustomerMessage {
    private String touser;
    private String msgtype = "voice";
    private Voice voice;

    public VoiceCustomerMessage(String openid, String mediaId){
        this.touser = openid;
        this.voice = new Voice(mediaId);
    }
}
