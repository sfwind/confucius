package com.iquanwai.confucius.mq;

import com.iquanwai.confucius.biz.domain.message.MQService;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQReceiver;
import com.iquanwai.confucius.web.pc.LoginUserService;
import com.rabbitmq.client.Channel;
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
    private MQService mqService;

    @PostConstruct
    public void init() {
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(null, TOPIC, ConfigUtils.getRabbitMQIp(), ConfigUtils.getRabbitMQPort());
        Channel channel = receiver.getChannel();
        logger.info(TOPIC + "通道建立");
        receiver.setAfterDealQueue(mqService::updateAfterDealOperation);
        Consumer<Object> consumer = getConsumer();
        receiver.listen(consumer);
        logger.info(TOPIC + "开启队列监听");
    }

    private Consumer<Object> getConsumer() {
        return msg -> {
            String message = msg.toString();
            logger.info("receive message {}", message);
            loginUserService.logout(message);
        };
    }
}


