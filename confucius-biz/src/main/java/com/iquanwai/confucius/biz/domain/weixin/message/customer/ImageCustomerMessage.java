package com.iquanwai.confucius.biz.domain.weixin.message.customer;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by justin on 17/7/8.
 */
@Data
public class ImageCustomerMessage {
    private String touser;
    private String msgtype = "image";
    private Image image;

    public ImageCustomerMessage(String openid, String mediaId){
        this.touser = openid;
        this.image = new Image(mediaId);
    }
}
