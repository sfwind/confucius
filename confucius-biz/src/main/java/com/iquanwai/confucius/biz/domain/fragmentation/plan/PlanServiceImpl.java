package com.iquanwai.confucius.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.confucius.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.PracticePlan;
import com.iquanwai.confucius.biz.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2016/12/29.
 */
@Service
public class PlanServiceImpl implements PlanService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private PracticePlanDao practicePlanDao;

    @Override
    public ImprovementPlan getRunningPlan(String openid) {
        return improvementPlanDao.loadRunningPlan(openid);
    }

    @Override
    public List<ImprovementPlan> loadUserPlans(String openId){
        return improvementPlanDao.loadUserPlans(openId);
    }

    @Override
    public List<ImprovementPlan> loadUserPlans(String openId, Integer problemId){
        return improvementPlanDao.loadUserPlans(openId).stream().filter(item -> item.getProblemId().equals(problemId)).collect(Collectors.toList());
    }

    @Override
    public List<PracticePlan> loadWorkPlanList(Integer planId) {
        List<PracticePlan> result = Lists.newArrayList();
        List<PracticePlan> temp = practicePlanDao.loadPracticePlan(planId);
        temp.forEach(item->{
            if (item.getType() == Constants.PracticeType.APPLICATION || item.getType() == Constants.PracticeType.CHALLENGE) {
                result.add(item);
            }
        });
        return result;
    }

    @Override
    public boolean hasProblemPlan(String openId, Integer problemId) {
        List<ImprovementPlan> improvementPlans = improvementPlanDao.loadUserPlans(openId);
        long count = improvementPlans.stream().filter(item -> item.getProblemId().equals(problemId)).count();
        return count > 0;
    }


}
