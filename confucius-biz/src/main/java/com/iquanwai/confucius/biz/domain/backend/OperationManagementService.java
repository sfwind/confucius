package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPractice;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPracticeDiscuss;
import com.iquanwai.confucius.biz.util.page.Page;

import java.util.List;

/**
 * Created by justin on 17/3/16.
 */
public interface OperationManagementService {

    /**
     * 根据应用id,获取应用训练提交记录
     * */
    List<ApplicationSubmit> loadApplicationSubmit(Integer practiceId, Page page);

    /**
     * 获取48小时内讨论区活跃的问题
     * */
    List<WarmupPractice> getLastTwoDayActivePractice();

    /**
     * 获取理解训练
     * */
    WarmupPractice getWarmupPractice(Integer practiceId);

    /**
     * 内容运营回复某个理解训练
     * @param openid 发表讨论的用户openid
     * @param warmupPracticeId 理解训练id
     * @param comment 讨论内容
     * @param repliedId 回复的讨论id
     * */
    void discuss(String openid, Integer warmupPracticeId, String comment, Integer repliedId);

    /**
     * 回复加精
     */
    void highlight(Integer discussId);
}
