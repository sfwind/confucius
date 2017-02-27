package com.iquanwai.confucius.job;

import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by nethunder on 2017/2/27.
 */
@Component
public class FragmentDailyDataJob {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private PracticeService practiceService;

    @Scheduled(cron="${fragmentDailyDataJob.cron}")
    public void work(){
        logger.info("FragmentDailyDataJob start");
        practiceService.fragmentDailyPracticeData();
        logger.info("FragmentDailyDataJob end");
    }
}
