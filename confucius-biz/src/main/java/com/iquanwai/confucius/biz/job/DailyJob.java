package com.iquanwai.confucius.biz.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by justin on 16/9/11.
 */
@Component
public class DailyJob {

    @Scheduled(cron="${dailyJob.cron}")
    public void courseProgress(){
//        System.out.println("haha");
    }
}
