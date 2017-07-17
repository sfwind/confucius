package com.iquanwai.confucius.biz.domain.weixin.message.callback;

import lombok.Data;

/**
 * Created by justin on 17/7/14.
 */
@Data
public class SubscribeEvent {
    private String scene;
    private String openid;
}
