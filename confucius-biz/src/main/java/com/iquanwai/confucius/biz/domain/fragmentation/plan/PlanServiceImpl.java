package com.iquanwai.confucius.biz.domain.fragmentation.plan;

import com.iquanwai.confucius.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.confucius.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.PracticePlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<ImprovementPlan> loadUserPlans(Integer profileId) {
        return improvementPlanDao.loadAllPlans(profileId);
    }

    @Override
    public List<ImprovementPlan> getPlans(Integer profileId) {
        return improvementPlanDao.loadPlans(profileId);
    }

    @Override
    public List<PracticePlan> loadPracticePlans(Integer planId) {
        return practicePlanDao.loadPracticePlan(planId);
    }

}
