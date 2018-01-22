package com.iquanwai.confucius.biz.domain.fragmentation.plan;

import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;

import java.util.List;

/**
 * Created by nethunder on 2016/12/29.
 */
public interface PlanService {

    /**
     * 获得用户的计划
     * @param profileId 用户id
     */
    List<ImprovementPlan> loadUserPlans(Integer profileId);
}
