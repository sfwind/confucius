package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.dao.fragmentation.ApplicationPracticeDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ApplicationSubmitDao;
import com.iquanwai.confucius.biz.dao.fragmentation.FragmentAnalysisDataDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.confucius.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepoImpl;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.Constants;
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
    @Autowired
    private FragmentAnalysisDataDao fragmentAnalysisDataDao;

    @Override
    public ApplicationPractice loadApplicationPractice(Integer id) {
        return applicationPracticeDao.load(ApplicationPractice.class, id);
    }

    @Override
    public ApplicationSubmit loadMineApplicationPractice(Integer planId, Integer applicationId, String openId,boolean create) {
        // 查询该应用练习
        ApplicationPractice applicationPractice = applicationPracticeDao.load(ApplicationPractice.class, applicationId);
        // 查询该用户是否提交
        ApplicationSubmit submit = applicationSubmitDao.load(applicationId, planId, openId);
        if (submit == null && create) {
            // 没有提交，生成
            submit = new ApplicationSubmit();
            submit.setOpenid(openId);
            submit.setPlanId(planId);
            submit.setApplicationId(applicationId);
            submit.setProblemId(applicationPractice.getProblemId());
            int submitId = applicationSubmitDao.insert(submit);
            submit.setId(submitId);
            submit.setUpdateTime(new Date());
            fragmentAnalysisDataDao.insertArticleViewInfo(ArticleViewInfo.initArticleViews(Constants.ViewInfo.Module.APPLICATION, submitId));
        }else{
            if(submit==null) {
                submit = new ApplicationSubmit();
            }
        }
        submit.setTopic(applicationPractice.getTopic());
        submit.setDescription(applicationPractice.getDescription());
        submit.setApplicationId(applicationPractice.getId());
        return submit;
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
        boolean result;
        int length = CommonUtils.removeHTMLTag(content).length();
        if(submit.getContent() == null){
            result = applicationSubmitDao.firstAnswer(id, content, length);
        } else {
            result = applicationSubmitDao.answer(id, content, length);
        }

        if (result && submit.getPointStatus() == 0) {
            // 修改应用任务记录
            ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, submit.getPlanId());
            if (plan != null) {
                improvementPlanDao.updateApplicationComplete(plan.getId());
            } else {
                logger.error("ImprovementPlan is not existed,planId:{}", submit.getPlanId());
            }
            logger.info("应用练习加分:{}", id);
            int applicationId = submit.getApplicationId();
            ApplicationPractice applicationPractice = applicationPracticeDao.load(ApplicationPractice.class, applicationId);

            Integer type;
            if(Knowledge.isReview(applicationPractice.getKnowledgeId())){
                type = PracticePlan.APPLICATION_REVIEW;
            }else{
                type = PracticePlan.APPLICATION;
            }

            PracticePlan practicePlan = practicePlanDao.loadPracticePlan(submit.getPlanId(),
                    submit.getApplicationId(), type);
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
