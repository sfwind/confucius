package com.iquanwai.confucius.biz.domain.course.introduction;

import com.iquanwai.confucius.biz.dao.course.CourseIntroductionDao;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.po.systematism.CourseIntroduction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/9/4.
 */
@Service
public class CourseIntroductionServiceImpl implements CourseIntroductionService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CourseIntroductionDao courseIntroductionDao;

    @Override
    public List<CourseIntroduction> loadAll() {
        List<CourseIntroduction> courses = courseIntroductionDao.loadAll(CourseIntroduction.class);

        return courses.stream().map(courseIntroduction -> {
            courseIntroduction.setIntro(null);
            return courseIntroduction;
        }).collect(Collectors.toList());
    }

    @Override
    public CourseIntroduction loadCourse(int courseId) {
        return courseIntroductionDao.getByCourseId(courseId);
    }

    @Override
    public List<CourseIntroduction> loadNotEntryCourses(List<ClassMember> classMemberList) {
        Assert.notNull(classMemberList, "学员信息不能为空");
        List<CourseIntroduction> courseList = loadAll();
        return courseList.stream().filter(course -> {
            for (ClassMember classMember : classMemberList) {
                if (classMember.getCourseId().equals(course.getCourseId())) {
                    return false;
                }
            }
            return true;
        }).filter(course -> !course.getHidden())
                .sorted((left,right)->right.getSequence()-left.getSequence())
                .collect(Collectors.toList());
    }
}
