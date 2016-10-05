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
     * @param classMember 学员详情
     * @param week 加载当前周的章节
     * */
    Course loadCourse(ClassMember classMember, int week);

    /**
     * 每天定时更新每个班级的进度
     * */
    void classProgress();

    /**
     * 设置学员的每一章节看到的页数
     * */
    void personalChapterPage(String openid, List<Chapter> chapters);

    /**
     * 毕业流程
     * @param classId 班级id
     * */
    void graduate(Integer classId);
}
