package com.iquanwai.confucius.mq;

import com.iquanwai.confucius.biz.domain.course.signup.MemberTypeManager;
import com.iquanwai.confucius.biz.domain.fragmentation.CacheService;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class PurchaseConfigReloadReceiver {

    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private MemberTypeManager memberTypeManager;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public final static String TOPIC = "purchase_configuration_reload";

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(null, TOPIC, (messageQueue) -> {
            String message = messageQueue.getMessage().toString();
            if ("PurchaseConfigReload".equals(message)) {
                cacheService.reloadBusinessCollegeConfig();
                cacheService.reloadMonthlyCampConfig();
                memberTypeManager.reload();
                logger.info("支付配置刷新成功");
            }
        });
    }

}
