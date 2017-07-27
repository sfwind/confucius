package com.iquanwai.confucius.biz.domain.message;


import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQDto;

/**
 * Created by nethunder on 2017/7/22.
 */
public interface MQService {

    void saveMQSendOperation(MQSendLog queue);

    void updateAfterDealOperation(RabbitMQDto msgId);
}
