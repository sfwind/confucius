package com.iquanwai.confucius.biz.po.common.message;

import lombok.Data;

/**
 * Created by nethunder on 2017/8/30.
 */
@Data
public class WechatMessage {
    public String openid;
    private String message;
    private String wxid;
}
