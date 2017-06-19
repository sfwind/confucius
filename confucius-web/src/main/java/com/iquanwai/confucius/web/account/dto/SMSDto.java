package com.iquanwai.confucius.web.account.dto;

import lombok.Data;

import java.util.Map;

/**
 * Created by nethunder on 2017/6/15.
 */
@Data
public class SMSDto {
    private Integer profileId;
    private String phone;
    private String content;
    private Map<String,String> replace;
}
