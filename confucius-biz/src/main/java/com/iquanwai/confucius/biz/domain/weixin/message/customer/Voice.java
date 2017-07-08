package com.iquanwai.confucius.biz.domain.weixin.message.customer;

import lombok.Data;

/**
 * Created by justin on 17/7/8.
 */
@Data
public class Voice {
    private String media_id;

    public Voice(String mediaId) {
        this.media_id = mediaId;
    }
}
