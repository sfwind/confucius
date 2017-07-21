package com.iquanwai.confucius.job;

import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by justin on 16/9/14.
 */
@Component
public class CloseOrderJob {
    @Autowired
    private PayService payService;

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Scheduled(cron="${closeOrderJob.cron}")
    public void work(){
//        logger.info("CloseOrderJob start");
//        payService.closeOrder();
//        logger.info("CloseOrderJob end");
    }
}
