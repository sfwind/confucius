package com.iquanwai.confucius.web.account.dto;

import lombok.Data;

/**
 * Created by nethunder on 2016/12/25.
 **/
@Data
public class AccountDto {
    private Integer role;
    private String weixinName;
    private String headimgUrl;
    private String key;
    private String callbackUrl;
}
