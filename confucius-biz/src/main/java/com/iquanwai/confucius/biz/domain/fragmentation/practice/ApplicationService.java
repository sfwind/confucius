package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.po.fragmentation.ApplicationPractice;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;

import java.util.List;

/**
 * Created by nethunder on 2017/1/13.
 */
public interface ApplicationService {
    /**
     * 获取应用练习
     *
     * @param applicationId 应用练习id
     */
    ApplicationPractice loadApplicationPractice(Integer applicationId);

    /**
     * 加载自己的应用练习任务
     *
     * @param planId        计划id
     * @param applicationId 应用练习id
     * @param profileId     用户id
     * @return 应用练习
     */
    ApplicationSubmit loadMineApplicationPractice(Integer planId, Integer applicationId, Integer profileId,
                                                  boolean create);

    /**
     * 获取应用练习的提交答案
     *
     * @param applicationId 应用练习id
     */
    List<ApplicationSubmit> loadApplicationSubmitList(Integer applicationId);

    /**
     * 获取提交记录
     *
     * @param id 提交id
     */
    ApplicationSubmit loadSubmit(Integer id);

    /**
     * 更新应用练习题干
     * @param applicationId id
     * @param topic 主题
     * @param description 描述
     * @param difficulty 难易度
     */
    Integer updateApplicationPractice(Integer applicationId, String topic, String description,int difficulty);

    /**
     * 插入应用题
     * @param applicationPractice
     * @return
     */
    int insertApplicationPractice(ApplicationPractice applicationPractice);
}
