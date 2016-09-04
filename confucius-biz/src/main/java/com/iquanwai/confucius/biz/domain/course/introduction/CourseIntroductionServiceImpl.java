package com.iquanwai.confucius.biz.domain.course.introduction;

import com.iquanwai.confucius.biz.dao.course.CourseDao;
import com.iquanwai.confucius.biz.dao.po.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by justin on 16/9/4.
 */
@Service
public class CourseIntroductionServiceImpl implements CourseIntroductionService {
    @Autowired
    private CourseDao courseDao;

    public List<Course> loadAll() {
        return courseDao.loadAll(Course.class);
    }

    public Course loadCourse(int courseId) {
        return courseDao.load(Course.class, courseId);
    }
}
