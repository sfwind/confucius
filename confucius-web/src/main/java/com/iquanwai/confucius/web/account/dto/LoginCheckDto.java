package com.iquanwai.confucius.web.account.dto;

import com.iquanwai.confucius.web.resolver.LoginUser;
import lombok.Data;

/**
 * Created by nethunder on 2016/12/20.
 */
@Data
public class LoginCheckDto {
    private String sessionId;
    private String status;
    private Integer error;
    private LoginUser loginUser;
    private String openId;
}
