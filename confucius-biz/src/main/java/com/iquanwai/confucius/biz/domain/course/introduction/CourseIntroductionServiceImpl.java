package com.iquanwai.confucius.biz.domain.course.introduction;

import com.iquanwai.confucius.biz.dao.course.CourseIntroductionDao;
import com.iquanwai.confucius.biz.po.CourseIntroduction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by justin on 16/9/4.
 */
@Service
public class CourseIntroductionServiceImpl implements CourseIntroductionService {
    @Autowired
    private CourseIntroductionDao courseIntroductionDao;

    public List<CourseIntroduction> loadAll() {
        return courseIntroductionDao.loadAll(CourseIntroduction.class);
    }

    public CourseIntroduction loadCourse(int courseId) {
        return courseIntroductionDao.getByCourseId(courseId);
    }
}
