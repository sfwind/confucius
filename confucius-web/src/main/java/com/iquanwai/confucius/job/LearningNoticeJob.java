package com.iquanwai.confucius.job;

import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.po.systematism.QuanwaiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by justin on 16/10/17.
 */
@Component
public class LearningNoticeJob {
    @Autowired
    private CourseProgressService courseProgressService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(cron="${learning.notice.cron}")
    public void work(){
        logger.info("LearningNoticeJob start");

        List<QuanwaiClass> quanwaiClassList = courseProgressService.loadActiveClass();
        for(QuanwaiClass quanwaiClass:quanwaiClassList){
            courseProgressService.noticeIncompleteMembers(quanwaiClass);
        }

        logger.info("LearningNoticeJob end");
    }
}
