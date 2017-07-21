package com.iquanwai.confucius.biz.domain.fragmentation.plan;

import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;
import com.iquanwai.confucius.biz.po.fragmentation.PracticePlan;

import java.util.List;

/**
 * Created by nethunder on 2016/12/29.
 */
public interface PlanService {

    /**
     * 获得正在进行的计划
     */
    ImprovementPlan getRunningPlan(String openid) ;

    /**
     * 获得用户的计划
     * @param openId 用户id
     */
    List<ImprovementPlan> loadUserPlans(String openId);

    /**
     * 获得用户的购买的计划
     * @param openId 用户id
     * @param problemId 难题id
     */
    ImprovementPlan loadUserPlan(String openId, Integer problemId);

    ImprovementPlan loadPlanByProblemId(Integer profileId, Integer problemId);

    /**
     * 获取该计划的作业
     * @param planId 计划id
     * @return 计划列表
     */
    List<PracticePlan> loadWorkPlanList(Integer planId);


    boolean hasProblemPlan(String openId, Integer problemId);

    Knowledge getKnowledge(Integer knowledgeId);

    List<Knowledge> getProblemKnowledgeList(Integer problemId);
}
