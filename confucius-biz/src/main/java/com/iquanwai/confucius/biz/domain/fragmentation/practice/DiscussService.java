package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.po.fragmentation.WarmupPracticeDiscuss;

import java.util.List;

/**
 * 评论service
 */
public interface DiscussService {

    /**
     * 获得当天的选择题评论
     * @return
     */
    public List<WarmupPracticeDiscuss> loadTodayDiscuss();



}
