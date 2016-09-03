package com.iquanwai.confucius.biz.domain.course.progress;

import com.iquanwai.confucius.biz.dao.po.Chapter;
import com.iquanwai.confucius.biz.dao.po.Page;

/**
 * Created by justin on 16/8/31.
 */
public interface CourseStudyService {
    /**
     * 获取章节页信息
     * @param openId
     * @param chapterId 课程章节id
     * @param page 课程页码
     * */
    Page loadPage(String openId, int chapterId, Integer page);

    /**
     * 获取章节信息
     * @param chapterId 课程章节id
     * */
    Chapter loadChapter(int chapterId);
}
