package com.iquanwai.confucius.web.weixin.dto;

import lombok.Data;

/**
 * Created by 三十文
 */
@Data
public class WeMiniCallback {

    private String state; // 随机值，供客户端调用
    private Long expireDate;
    private Boolean firstLogin; // 是否是第一次登录

}
