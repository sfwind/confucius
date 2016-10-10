package com.iquanwai.confucius.biz.service;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 16/10/9.
 */
public class CourseStudyServiceTest extends TestBase {
    @Autowired
    private CourseStudyService courseStudyService;

    @Test
    public void testHomeworkSubmitted(){
        courseStudyService.submitHomework("aaaa", "o5h6ywlXxHLmoGrLzH9Nt7uyoHbM", 1);
    }
}
