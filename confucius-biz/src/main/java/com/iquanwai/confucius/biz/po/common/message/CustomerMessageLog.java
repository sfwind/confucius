package com.iquanwai.confucius.biz.po.common.message;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/8/10.
 */
@Data
public class CustomerMessageLog {

    private Integer id;
    private String openId;  //用户openid
    private Date publishTime; //发送时间
    private String comment; //消息发送目的
    private String contentHash; // 发送内容的 hash 值
    private Integer forwardlyPush; // 用户无触发推送
    private Integer validPush; // 有效的推送
    private String source; //跟踪消息打开率

}
