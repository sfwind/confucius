package com.iquanwai.confucius.mq;

import com.iquanwai.confucius.biz.domain.fragmentation.CacheService;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MonthlyCampConfigReloadReceiver {

    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private CacheService cacheService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public final static String TOPIC = "monthly_camp_configuration_reload";

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(null, TOPIC, (messageQueue) -> {
            String message = messageQueue.getMessage().toString();
            if ("campConfigReload".equals(message)) {
                cacheService.reloadMonthlyCampConfig();
                logger.info("训练营配置刷新成功");
            }
        });
    }

}
