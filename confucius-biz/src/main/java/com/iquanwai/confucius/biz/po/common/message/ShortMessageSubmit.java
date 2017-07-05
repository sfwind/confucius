package com.iquanwai.confucius.biz.po.common.message;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/6/18.
 * 短信提交表
 * 这个表每条记录都只能发送给单个用户
 */
@Data
public class ShortMessageSubmit {
    private Integer id;
    private String msgId;
    private Integer profileId;
    private String phones;
    private String content;
    private String sign;
    private Date sendTime;
    private String result;
    private String description;
    private String failPhones;
    private Integer type;


    private String account;
    private String password;

}
