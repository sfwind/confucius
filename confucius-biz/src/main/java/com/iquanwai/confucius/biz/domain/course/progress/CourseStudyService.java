package com.iquanwai.confucius.biz.domain.course.progress;

import com.iquanwai.confucius.biz.po.*;

import java.util.List;

/**
 * Created by justin on 16/8/31.
 */
public interface CourseStudyService {
    /**
     * 获取章节页信息
     * @param openid
     * @param chapterId 课程章节id
     * @param page 课程页码
     * */
    Page loadPage(String openid, Integer chapterId, Integer page, Boolean lazyLoad);

    /**
     * 获取章节信息
     * @param openid
     * @param chapterId 课程章节id
     * */
    Chapter loadChapter(String openid, Integer chapterId);


    /**
     * 获取问题信息
     * @param openid
     * @param questionId 问题id
     * */
    Question loadQuestion(String openid, Integer questionId);

    /**
     * 获取作业信息
     * @param openid
     * @param homeworkId 作业id
     * */
    Homework loadHomework(String openid, Integer homeworkId);

    /**
     * 获取作业提交信息
     * @param url url
     * */
    HomeworkSubmit loadHomework(String url);

    /**
     * 获取提交的作业
     * @param homeworkId 作业id
     * */
    List<HomeworkSubmit> loadSubmittedHomework(Integer homeworkId);

    /**
     * 作业提交
     * @param content 提交答案
     * @param openid
     * @param homeworkId 作业id
     * */
    void submitHomework(String content, String openid, Integer homeworkId);

    /**
     * 选择题提交
     * @param choiceList 提交选项列表
     * @param openid
     * @param questionId 选择题id
     * */
    boolean submitQuestion(String openid, Integer questionId, List<Integer> choiceList);

    /**
     * 章节结束
     * @param openid
     * @param chapterId 章节id
     * */
    void completeChapter(String openid, Integer chapterId);

    /**
     * 作业批改
     * @param openid 学员id
     * @param classId 班级id
     * @param homeworkId 作业id
     * @param excellent 优秀
     * @param fail 不及格
     * */
    void remark(String openid, Integer classId, Integer homeworkId, boolean excellent, boolean fail);

    /**
     * 记录看到第几页
     * */
    void markPage(String openid, Integer chapterId, Integer pageSequence);

    /**
     * 获取周主题
     * */
    CourseWeek loadCourseWeek(Integer courseId, Integer week);

    /**
     * 获取课前准备的第一章节信息
     * @param courseId 课程id
     * */
    Chapter loadFirstPreparedChapter(Integer courseId);

    /**
     * 重新加载问题
     * */
    void reloadQuestion();
}
