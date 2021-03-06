package com.iquanwai.confucius.biz.domain.fragmentation.plan;

import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.PracticePlan;

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

    /**
     * 获得所有的计划（包括已经删除的）
     * @param profileId
     * @return
     */
    List<ImprovementPlan> getPlans(Integer profileId);


    List<PracticePlan> loadPracticePlans(Integer PlanId);

}
