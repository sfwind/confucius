package com.iquanwai.confucius.biz.domain.course.introduction;

import com.iquanwai.confucius.biz.dao.course.CourseIntroductionDao;
import com.iquanwai.confucius.biz.po.ClassMember;
import com.iquanwai.confucius.biz.po.CourseIntroduction;
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
    @Autowired
    private CourseIntroductionDao courseIntroductionDao;

    public List<CourseIntroduction> loadAll() {
        List<CourseIntroduction> courses = courseIntroductionDao.loadAll(CourseIntroduction.class);

        return courses.stream().map(courseIntroduction -> {
            courseIntroduction.setIntro(null);
            return courseIntroduction;
        }).collect(Collectors.toList());
    }

    public CourseIntroduction loadCourse(int courseId) {
        return courseIntroductionDao.getByCourseId(courseId);
    }

    public List<CourseIntroduction> loadNotEntryCourses(List<ClassMember> classMemberList) {
        Assert.notNull(classMemberList, "学员信息不能为空");
        List<CourseIntroduction> courseList = loadAll();
        return courseList.stream().filter(course -> {
            for(ClassMember classMember:classMemberList) {
                if(classMember.getCourseId().equals(course.getCourseId())){
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }
}
