package com.iquanwai.confucius.biz.domain.course.introduction;

import com.iquanwai.confucius.biz.po.CourseIntroduction;

import java.util.List;

/**
 * Created by justin on 16/9/4.
 */
public interface CourseIntroductionService {
    /**
     * 获取所有课程的介绍
     * */
    List<CourseIntroduction> loadAll();

    /**
     * 获取课程信息，不加载章节信息
     * @param courseId 课程id
     * */
    CourseIntroduction loadCourse(int courseId);
}
