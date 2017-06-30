package com.iquanwai.confucius.biz.domain.message;

import lombok.Data;

/**
 * Created by nethunder on 2017/6/16.
 */
@Data
public class SMSConfig {
    private String account;
    private String password;
    private String sign;
}
