package com.iquanwai.confucius.biz.po.common.message;

import lombok.Data;

/**
 * Created by nethunder on 2017/7/22.
 */
@Data
public class MessageQueue {
    private Integer id;
    private String msgId;
    private String topic;
    private String queue;
    private Integer status;
    private String message;
    private String consumerIp;
}
