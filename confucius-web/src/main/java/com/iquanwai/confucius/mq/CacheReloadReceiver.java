package com.iquanwai.confucius.mq;

import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.course.signup.RiseMemberCountRepo;
import com.iquanwai.confucius.biz.domain.course.signup.RiseMemberTypeRepo;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.permission.PermissionService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQReceiver;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Created by justin on 17/4/25.
 */
@Service
public class CacheReloadReceiver {
    public final static String TOPIC = "confucius_resource_reload";

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private CourseStudyService courseStudyService;
    @Autowired
    private SignupService signupService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private RiseMemberTypeRepo riseMemberTypeRepo;
    @Autowired
    private RiseMemberCountRepo riseMemberCountRepo;

    @PostConstruct
    public void init(){
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(null, TOPIC, ConfigUtils.getRabbitMQIp(), ConfigUtils.getRabbitMQPort());
        Channel channel = receiver.getChannel();
        logger.info("通道建立");
        Consumer consumer = getConsumer(channel);
        receiver.listen(consumer);
        logger.info("开启队列监听");
    }


    private Consumer getConsumer(Channel channel){
        return new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body);
                logger.info("receive message {}", message);
                switch (message){
                    case "class":
                        signupService.reloadClass();
                        courseStudyService.reloadQuestion();
                        break;
                    case "permission":
                        permissionService.initPermission();
                        break;
                    case "region":
                        accountService.loadAllProvinces();
                        accountService.loadCities();
                        break;
                    case "rise_member":
                        riseMemberTypeRepo.reload();
                        riseMemberCountRepo.reload();
                }

            }
        };
    }
}
