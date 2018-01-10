package com.iquanwai.confucius.biz.po;

import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * Created by justin on 16/8/13.
 */
@Data
@Alias("callback")
public class Callback {

    private String callbackUrl; // 回调地址
    private String state; // 随机值，供客户端调用
    private String accessToken; // 服务号 accessToken
    private String refreshToken; // 服务号 刷新 accessToken 使用
    private String pcAccessToken; // PC accessToken
    private String weMiniAccessToken; // 小程序 AccessToken
    private String unionId; // 微信公众平台 unionId
    private String openid;  // 服务号 openId
    private String pcOpenid;    // PC OpenId
    private String weMiniOpenid; // 小程序 OpenId


    // 非 db
    private Long expireDate;
}
