package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.dao.fragmentation.ApplicationPracticeDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ApplicationSubmitDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.confucius.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepoImpl;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/1/13.
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private PointRepo pointRepo;

    @Override
    public ApplicationPractice loadApplicationPractice(Integer id) {
        return applicationPracticeDao.load(ApplicationPractice.class, id);
    }

    @Override
    public ApplicationPractice loadMineApplicationPractice(Integer planId, Integer applicationId, String openId) {
        // 查询该应用训练
        ApplicationPractice applicationPractice = applicationPracticeDao.load(ApplicationPractice.class, applicationId);
        // 查询该用户是否提交
        ApplicationSubmit submit = applicationSubmitDao.load(applicationId, planId, openId);
        if (submit == null) {
            // 没有提交，生成
            submit = new ApplicationSubmit();
            submit.setOpenid(openId);
            submit.setPlanId(planId);
            submit.setApplicationId(applicationId);
            int submitId = -1;
            submitId = applicationSubmitDao.insert(submit);
            submit.setId(submitId);
            submit.setUpdateTime(new Date());
        }
        applicationPractice.setSubmitUpdateTime(submit.getUpdateTime());
        applicationPractice.setPlanId(submit.getPlanId());
        applicationPractice.setContent(submit.getContent());
        applicationPractice.setSubmitId(submit.getId());
        return applicationPractice;
    }

    @Override
    public List<ApplicationSubmit> loadApplicationSubmitList(Integer applicationId) {
        return applicationSubmitDao.load(applicationId);
    }

    @Override
    public Boolean submit(Integer id, String content) {
        ApplicationSubmit submit = applicationSubmitDao.load(ApplicationSubmit.class, id);
        if (submit == null) {
            logger.error("submitId {} is not existed", id);
            return false;
        }
        boolean result = applicationSubmitDao.answer(id, content);
        if (result && submit.getPointStatus() == 0) {
            // 修改应用任务记录
            ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, submit.getPlanId());
            if (plan != null) {
                improvementPlanDao.updateApplicationComplete(plan.getId());
            } else {
                logger.error("ImprovementPlan is not existed,planId:{}", submit.getPlanId());
            }
            logger.info("应用训练加分:{}", id);
            PracticePlan practicePlan = practicePlanDao.loadPracticePlan(submit.getPlanId(),
                    submit.getApplicationId(), PracticePlan.APPLICATION);
            if (practicePlan != null) {
                practicePlanDao.complete(practicePlan.getId());
                Integer point = PointRepoImpl.score.get(applicationPracticeDao.load(ApplicationPractice.class, submit.getApplicationId()).getDifficulty());
                // 查看难度，加分
                pointRepo.risePoint(submit.getPlanId(),point);
                // 修改status
                applicationSubmitDao.updatePointStatus(id);
                pointRepo.riseCustomerPoint(submit.getOpenid(),point);
            }
        }
        return result;
    }

    @Override
    public ApplicationSubmit loadSubmit(Integer id){
        return applicationSubmitDao.load(ApplicationSubmit.class,id);
    }
}
