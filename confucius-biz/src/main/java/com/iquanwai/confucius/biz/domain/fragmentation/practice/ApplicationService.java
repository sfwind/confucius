package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.po.fragmentation.ApplicationPractice;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;

import java.util.List;

/**
 * Created by nethunder on 2017/1/13.
 */
public interface ApplicationService {
    ApplicationPractice loadApplicationPractice(Integer id);

    /**
     * 加载自己的应用训练任务
     * @param planId 计划id
     * @param applicationId 应用训练id
     * @param OpenId openId
     * @return 应用训练
     */
    ApplicationPractice loadMineApplicationPractice(Integer planId, Integer applicationId, String OpenId);


    List<ApplicationSubmit> loadApplicationSubmitList(Integer applicationId);

    Boolean submit(Integer id,String content);

    ApplicationSubmit loadSubmit(Integer id);
}
