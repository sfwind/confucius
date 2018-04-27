package com.iquanwai.confucius.mq;

import com.iquanwai.confucius.biz.domain.course.signup.MemberTypeManager;
import com.iquanwai.confucius.biz.domain.permission.PermissionService;
import com.iquanwai.confucius.biz.domain.weixin.message.callback.CallbackMessageService;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQDto;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

/**
 * Created by justin on 17/4/25.
 */
@Service
public class CacheReloadReceiver {
    public final static String TOPIC = "confucius_resource_reload";
    public final static String RISE_TOPIC = "rise_resource_reload";

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private CallbackMessageService callbackMessageService;
    @Autowired
    private MemberTypeManager memberTypeManager;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    @PostConstruct
    public void init(){
        rabbitMQFactory.initReceiver(null, TOPIC, getConsumer());
    }

    private Consumer<RabbitMQDto> getConsumer(){
        return queueMessage -> {
            String message = queueMessage.getMessage().toString();
            logger.info("receive message {}", message);
            switch (message) {
                case "permission":
                    permissionService.reloadPermission();
                    break;
                case "weixin_message":
                    callbackMessageService.reload();
                    break;
                case "rise_member":
                    memberTypeManager.reload();
                    break;
                default:
                    logger.error("异常，获取cacheReloadMq数据异常:{}", message);
            }
        };
    }
}
