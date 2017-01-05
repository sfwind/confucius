package com.iquanwai.confucius.biz.domain.course.introduction;

import com.iquanwai.confucius.biz.dao.course.CourseIntroductionDao;
import com.iquanwai.confucius.biz.po.ClassMember;
import com.iquanwai.confucius.biz.po.CourseIntroduction;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Comparator;
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
        // 在开放的训练课程中，按照课程最近一次的开启时间，逆序排列（越晚开课的，越在上方）；和导入顺序无关
        List<CourseIntroduction> tempList = courseList.stream().filter(course -> {
            for (ClassMember classMember : classMemberList) {
                if (classMember.getCourseId().equals(course.getCourseId())) {
                    return false;
                }
            }
            return true;
        }).filter(course -> !course.getHidden()).collect(Collectors.toList());
        // 根据courseId，查询对应的最大开启时间
        tempList.sort(new Comparator<CourseIntroduction>() {
            @Override
            public int compare(CourseIntroduction leftCourse, CourseIntroduction rightCourse) {
                try{
                    long leftUpdateTime = leftCourse.getUpdateTime().getTime();
                    long rightUpdateTime = rightCourse.getUpdateTime().getTime();
                    return rightUpdateTime - leftUpdateTime == 0 ? 0 : rightUpdateTime - leftUpdateTime > 0 ? 1 : -1;
                } catch (NullPointerException e){
                    logger.error(e.getLocalizedMessage());
                    return 0;
                }
            }
        });
        return tempList;
    }
}
