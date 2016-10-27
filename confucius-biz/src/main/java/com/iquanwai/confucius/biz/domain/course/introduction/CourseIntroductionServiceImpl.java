package com.iquanwai.confucius.biz.domain.course.introduction;

import com.iquanwai.confucius.biz.dao.course.CourseIntroductionDao;
import com.iquanwai.confucius.biz.po.ClassMember;
import com.iquanwai.confucius.biz.po.CourseIntroduction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.List;

/**
 * Created by justin on 16/9/4.
 */
@Service
public class CourseIntroductionServiceImpl implements CourseIntroductionService {
    @Autowired
    private CourseIntroductionDao courseIntroductionDao;

    public List<CourseIntroduction> loadAll() {
        List<CourseIntroduction> courses = courseIntroductionDao.loadAll(CourseIntroduction.class);
        for(CourseIntroduction courseIntroduction:courses){
            //intro信息太大,去掉
            courseIntroduction.setIntro(null);
        }
        return courses;
    }

    public CourseIntroduction loadCourse(int courseId) {
        return courseIntroductionDao.getByCourseId(courseId);
    }

    public List<CourseIntroduction> loadNotEntryCourses(List<ClassMember> classMemberList) {
        Assert.notNull(classMemberList, "学员信息不能为空");
        List<CourseIntroduction> courseList = loadAll();
        for(ClassMember classMember:classMemberList) {
            for (Iterator<CourseIntroduction> it = courseList.iterator(); it.hasNext(); ) {
                CourseIntroduction courseIntroduction = it.next();

                if(classMember.getCourseId().equals(courseIntroduction.getCourseId())){
                    it.remove();
                }
            }
        }
        return courseList;
    }
}
