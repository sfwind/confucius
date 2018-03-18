package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.po.fragmentation.WarmupPracticeDiscuss;

import java.util.List;

/**
 * 评论service
 */
public interface DiscussService {

    /**
     * 获得某天的选择题评论
     * @return
     */
    List<WarmupPracticeDiscuss> loadCurrentDayDiscuss(String currentDate);



    /**
     * 获取该评论的所有回复
     * @param replys
     * @return
     */
   List<WarmupPracticeDiscuss> loadByReplys(List<Integer> replys);

    /**
     * 获取某天选择题对应的评论
     * @param practiceId
     * @param currentDate
     * @return
     */
   List<WarmupPracticeDiscuss> loadTargetDiscuss(Integer practiceId,String currentDate);
}
