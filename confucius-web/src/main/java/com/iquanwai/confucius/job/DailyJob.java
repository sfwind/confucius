package com.iquanwai.confucius.job;

import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by justin on 16/9/11.
 */
@Component
public class DailyJob {
    @Autowired
    private CourseProgressService courseProgressService;
    @Autowired
    private SignupService signupService;

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Scheduled(cron="${dailyJob.cron}")
    public void work(){
        logger.info("DailyJob start");
        courseProgress();
        closeClass();
        noticeBeforeMemberClose();
        signupService.reloadClass();
        logger.info("DailyJob end");
    }

    private void noticeBeforeMemberClose() {
        courseProgressService.noticeWillCloseMember();
    }

    private void courseProgress(){
        courseProgressService.classProgress();
    }

    private void closeClass(){
        courseProgressService.closeClassEntry();
    }
}
