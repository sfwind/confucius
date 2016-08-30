package com.iquanwai.confucius.biz.domain.course.progress;

import com.iquanwai.confucius.biz.dao.po.ClassMember;
import com.iquanwai.confucius.biz.dao.po.Course;

/**
 * Created by justin on 16/8/29.
 */
public interface CourseProgressService {
    /**
     * 获取用户正在就读的班级信息
     * */
    ClassMember loadActiveCourse(String openid, Integer courseId);

    /**
     * @param courseId 课程id
     * @param week 加载当前周的章节
     * @param personalProgress 个人当前进度
     * @param classProgress 课程进度
     * */
    Course loadCourse(int courseId, int week, int personalProgress,int classProgress);
}
