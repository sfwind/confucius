package com.iquanwai.confucius.job;

import com.iquanwai.confucius.biz.domain.course.operational.OperationalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by justin on 16/10/11.
 */
@Component
public class AngelJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private OperationalService operationalService;

    @Scheduled(cron="${angelJob.cron}")
    public void work(){
        logger.info("AngelJob start");
        operationalService.angelAssign();
        logger.info("AngelJob end");
    }
}
