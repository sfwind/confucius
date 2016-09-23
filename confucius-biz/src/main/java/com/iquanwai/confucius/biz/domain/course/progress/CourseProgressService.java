package com.iquanwai.confucius.biz.domain.course.progress;

import com.iquanwai.confucius.biz.po.Chapter;
import com.iquanwai.confucius.biz.po.ClassMember;
import com.iquanwai.confucius.biz.po.Course;
import com.iquanwai.confucius.biz.po.CourseWeek;

import java.util.List;

/**
 * Created by justin on 16/8/29.
 */
public interface CourseProgressService {
    /**
     * 获取用户正在就读的班级信息
     * @param openid openid
     * @param courseId 课程id
     * */
    ClassMember loadActiveCourse(String openid, Integer courseId);

    /**
     * 获取课程信息
     * @param courseId 课程id
     * @param week 加载当前周的章节
     * @param personalProgress 个人当前进度
     * @param classProgress 课程进度
     * */
    Course loadCourse(int courseId, int week, List<Integer> personalProgress,int classProgress);

    /**
     * 每天定时更新每个班级的进度
     * */
    void classProgress();

    /**
     * 设置学员的每一章节看到的页数
     * */
    void personalChapterPage(String openid, List<Chapter> chapters);

    /**
     * 获取周主题
     * */
    CourseWeek loadCourseWeek(Integer courseId, Integer week);
}
