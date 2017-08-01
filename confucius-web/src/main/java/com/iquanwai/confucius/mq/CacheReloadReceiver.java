package com.iquanwai.confucius.mq;

import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.course.signup.RiseMemberCountRepo;
import com.iquanwai.confucius.biz.domain.course.signup.RiseMemberTypeRepo;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.message.MQService;
import com.iquanwai.confucius.biz.domain.permission.PermissionService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.callback.CallbackMessageService;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQReceiver;
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
    @Autowired
    private CallbackMessageService callbackMessageService;
    @Autowired
    private MQService mqService;

    @PostConstruct
    public void init(){
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(null, TOPIC);
        logger.info(TOPIC + "通道建立");
        receiver.setAfterDealQueue(mqService::updateAfterDealOperation);
        Consumer<Object> consumer = getConsumer();
        receiver.listen(consumer);
        logger.info(TOPIC + "开启队列监听");
    }


    private Consumer<Object> getConsumer(){
        return queueMessage -> {
            String message = queueMessage.toString();
            logger.info("receive message {}", message);
            switch (message) {
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
                    break;
                case "weixin_message":
                    callbackMessageService.reload();
                    break;
                default:
                    logger.error("异常，获取cacheReloadMq数据异常:{}", message);
            }
        };
    }
}
