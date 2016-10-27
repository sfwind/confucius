package com.iquanwai.confucius.biz.domain.course.progress;

import com.iquanwai.confucius.biz.po.*;

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
     * 获取用户正在就读的所有班级信息
     * @param openid openid
     * */
    List<ClassMember> loadActiveCourse(String openid);

    /**
     * 获取课程信息
     * @param classMember 学员详情
     * @param week 加载当前周的章节
     * */
    Course loadCourse(ClassMember classMember, int week);

    /**
     * 获取课程信息
     * @param courseId 课程id
     * */
    Course loadCourse(Integer courseId);

    /**
     * 每天定时更新每个班级的进度
     * */
    void classProgress();

    /**
     * 设置学员的每一章节看到的页数
     * @param openid 学员id
     * @param chapters 章节列表
     * */
    void personalChapterPage(String openid, List<Chapter> chapters);

    /**
     * 毕业流程
     * @param classId 班级id
     * */
    void graduate(Integer classId);

    /**
     * 每天定时关闭次日开班班级的报名
     * */
    void closeClassEntry();

    /**
     * 获取正在上课的班级
     * */
    List<QuanwaiClass> loadActiveClass();


    /**
     * 通知未完成学习任务的学员
     * @param quanwaiClass 班级
     * */
    void noticeIncompleteMembers(QuanwaiClass quanwaiClass);

    /**
     * 获取周主题
     * @param courseId 课程id
     * @param week 周数
     * */
    CourseWeek loadCourseWeek(Integer courseId, Integer week);

    /**
     * 生成证书上的文案
     * @param classMember 学员信息
     * */
    String certificateComment(ClassMember classMember);
}
