package com.iquanwai.confucius.biz.domain.course.progress;

import com.iquanwai.confucius.biz.dao.po.*;

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
    Page loadPage(String openid, int chapterId, Integer page);

    /**
     * 获取章节信息
     * @param openid
     * @param chapterId 课程章节id
     * */
    Chapter loadChapter(String openid, int chapterId);


    /**
     * 获取问题信息
     * @param openid
     * @param questionId 问题id
     * */
    Question loadQuestion(String openid, int questionId);

    /**
     * 获取作业信息
     * @param openid
     * @param homeworkId 作业id
     * */
    Homework loadHomework(String openid, int homeworkId);

    /**
     * 获取作业提交信息
     * @param url url
     * */
    HomeworkSubmit loadHomework(String url);

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
    void submitQuestion(String openid, Integer questionId, List<Integer> choiceList);

    /**
     * 章节结束
     * @param openid
     * @param chapterId 章节id
     * */
    void completeChapter(String openid, Integer chapterId);
}
