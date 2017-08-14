package com.iquanwai.confucius.mq;

import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQDto;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.confucius.web.pc.LoginUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

/**
 * Created by xfduan on 2017/6/30.
 */
@Service
public class CustomerReceiver {

    public static final String TOPIC = "customer";

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private LoginUserService loginUserService;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(null, TOPIC, getConsumer());
    }

    private Consumer<RabbitMQDto> getConsumer() {
        return msg -> {
            String message = msg.getMessage().toString();
            logger.info("receive message {}", message);
            loginUserService.logout(message);
        };
    }
}


