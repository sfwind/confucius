package com.iquanwai.confucius.biz.util.rabbitmq;

import lombok.Data;

/**
 * Created by nethunder on 2017/7/23.
 */
@Data
public class RabbitMQDto {
    private String msgId;
    private String queue; // 接收时所用的队列名
    private Object message;
    private String topic;
}
