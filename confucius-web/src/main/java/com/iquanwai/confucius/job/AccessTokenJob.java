package com.iquanwai.confucius.job;

import com.iquanwai.confucius.biz.domain.weixin.accessToken.AccessTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by nethunder on 2017/1/5.
 */
@Component
public class AccessTokenJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private AccessTokenService accessTokenService;

    @Scheduled(cron="${accessTokenJob.cron}")
    public void work(){
//        logger.info("AccessTokenJob start");
//        accessTokenService.refreshAccessToken(true);
//        logger.info("AccessTokenJob end");
    }
}
