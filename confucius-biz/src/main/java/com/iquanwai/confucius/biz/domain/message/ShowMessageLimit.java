package com.iquanwai.confucius.biz.domain.message;

/**
 * Created by nethunder on 2017/6/14.
 */
public class ShowMessageLimit {
    private Integer profileId; // id
    private Integer sendCount; //已发送数量
    private Integer Expired; //五分钟之内只能发送多少条
    private Integer limit; //条数限制
}
