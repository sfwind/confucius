package com.iquanwai.confucius.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.iquanwai.confucius.biz.dao.fragmentation.AuditionClassMemberDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.confucius.biz.dao.fragmentation.KnowledgeDao;
import com.iquanwai.confucius.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ProblemScheduleDao;
import com.iquanwai.confucius.biz.po.fragmentation.AuditionClassMember;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;
import com.iquanwai.confucius.biz.po.fragmentation.PracticePlan;
import com.iquanwai.confucius.biz.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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
    @Autowired
    private KnowledgeDao knowledgeDao;
    @Autowired
    private ProblemScheduleDao problemScheduleDao;
    @Autowired
    private AuditionClassMemberDao auditionClassMemberDao;

    @Override
    public ImprovementPlan getRunningPlan(String openid) {
        return improvementPlanDao.loadRunningPlan(openid);
    }

    @Override
    public List<ImprovementPlan> loadUserPlans(String openId){
        return improvementPlanDao.loadUserPlans(openId);
    }

    @Override
    public ImprovementPlan loadUserPlan(String openId, Integer problemId){
        return improvementPlanDao.loadPlanByProblemId(openId, problemId);
    }

    @Override
    public ImprovementPlan loadPlanByProblemId(Integer profileId,Integer problemId){
        return improvementPlanDao.loadPlanByProblemId(profileId, problemId);
    }

    @Override
    public List<PracticePlan> loadWorkPlanList(Integer planId) {
        List<PracticePlan> result = Lists.newArrayList();
        List<PracticePlan> temp = practicePlanDao.loadPracticePlan(planId);
        temp.forEach(item->{
            if (item.getType() == Constants.PracticeType.APPLICATION ||
                    item.getType() == Constants.PracticeType.APPLICATION_REVIEW ||
                    item.getType() == Constants.PracticeType.CHALLENGE) {
                result.add(item);
            }
        });
        return result;
    }

    @Override
    public boolean hasProblemPlan(String openId, Integer problemId) {
        ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(openId, problemId);
        return improvementPlan !=null;
    }

    @Override
    public Knowledge getKnowledge(Integer knowledgeId) {
        return knowledgeDao.load(Knowledge.class,knowledgeId);
    }

    @Override
    public List<Knowledge> getProblemKnowledgeList(Integer problemId){
        Set<Integer> knowledgeIds = Sets.newHashSet();
        problemScheduleDao.loadProblemSchedule(problemId).forEach(item -> knowledgeIds.add(item.getKnowledgeId()));
        List<Knowledge> list = Lists.newArrayList();
        knowledgeIds.forEach(item -> list.add(knowledgeDao.load(Knowledge.class, item)));
        return list;
    }

    @Override
    public AuditionClassMember getAuditionClassMember(Integer profileId) {
        return auditionClassMemberDao.loadByProfileId(profileId);
    }

    @Override
    public List<ImprovementPlan> getUserPlans(Integer profileId) {
        return improvementPlanDao.loadAllPlans(profileId);
    }

}
