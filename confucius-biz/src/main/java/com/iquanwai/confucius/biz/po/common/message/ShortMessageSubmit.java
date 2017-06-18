package com.iquanwai.confucius.biz.po.common.message;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/6/18.
 */
@Data
public class ShortMessageSubmit {
    private Integer id;
    private String msgId;
    private Integer profileId;
    private String phones;
    private String content;
    private String sign;
    private String subcode;
    private Date sendTime;
    private String result;
    private String description;
    private String failPhones;


    private String account;
    private String password;

}
