package com.iquanwai.confucius.mq;

import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQReceiver;
import com.iquanwai.confucius.web.pc.LoginUserService;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by xfduan on 2017/6/30.
 */
@Service
public class CustomerReceiver {

    public static final String TOPIC = "customer";

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private LoginUserService loginUserService;

    @PostConstruct
    public void init() {
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(null, TOPIC, ConfigUtils.getRabbitMQIp(), ConfigUtils.getRabbitMQPort());
        Channel channel = receiver.getChannel();
        logger.info(TOPIC + "通道建立");
        Consumer consumer = getConsumer(channel);
        receiver.listen(consumer);
        logger.info(TOPIC + "开启队列监听");
    }

    private Consumer getConsumer(Channel channel) {
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {
                String message = new String(body);
                logger.info("receive message {}", message);
                switch(message) {
                    case "logout":
                        loginUserService.logout(message);
                }
            }
        };
    }


}
