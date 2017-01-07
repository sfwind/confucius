package com.iquanwai.confucius.biz.domain.fragmentation.plan;

import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;

import java.util.List;

/**
 * Created by nethunder on 2016/12/29.
 */
public interface PlanService {
    public ImprovementPlan getRunningPlan(String openid) ;

    List<ImprovementPlan> loadUserPlans(String openId);
}
