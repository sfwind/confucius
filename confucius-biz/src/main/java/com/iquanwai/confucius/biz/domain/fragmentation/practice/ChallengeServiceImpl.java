package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.dao.fragmentation.ChallengePracticeDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ChallengeSubmitDao;
import com.iquanwai.confucius.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengePractice;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengeSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.PracticePlan;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by nethunder on 2017/1/13.
 */
@Service
public class ChallengeServiceImpl implements ChallengeService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ChallengePracticeDao challengePracticeDao;
    @Autowired
    private ChallengeSubmitDao challengeSubmitDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private PointRepo pointRepo;

    @Override
    public ChallengePractice loadChallengePractice(Integer id) {
        return challengePracticeDao.load(ChallengePractice.class, id);
    }

    @Override
    public ChallengePractice loadMineChallengePractice(Integer planId, Integer challengeId, String openId){
        ChallengePractice challengePractice = challengePracticeDao.load(ChallengePractice.class,challengeId);
        // 查询该用户是否提交
        ChallengeSubmit submit = challengeSubmitDao.load(challengeId, planId, openId);
        if(submit==null){
            // 没有提交，生成
            submit = new ChallengeSubmit();
            submit.setOpenid(openId);
            submit.setPlanId(planId);
            submit.setChallengeId(challengeId);
            int submitId = -1;
            submitId = challengeSubmitDao.insert(submit);
            submit.setId(submitId);
            submit.setUpdateTime(new Date());
        }
        challengePractice.setSubmitUpdateTime(submit.getUpdateTime());
        challengePractice.setPlanId(submit.getPlanId());
        challengePractice.setContent(submit.getContent());
        challengePractice.setSubmitId(submit.getId());
        return challengePractice;
    }


    @Override
    public Boolean submit(Integer id, String content) {
        ChallengeSubmit submit = challengeSubmitDao.load(ChallengeSubmit.class, id);
        if (submit == null) {
            logger.error("submitId {} is not existed", id);
            return false;
        }
        boolean result = challengeSubmitDao.answer(id, content);
        ;
        if (result) {
            // 修改挑战任务记录
            PracticePlan practicePlan = practicePlanDao.loadPracticePlan(submit.getPlanId(), submit.getChallengeId(), Constants.PracticeType.CHALLENGE);
            if (practicePlan != null) {
                practicePlanDao.complete(practicePlan.getId());
            } else {
                logger.error("practicePlan is not existed,planId:{},challengeId:{},type:{}", submit.getPlanId(), submit.getChallengeId(), Constants.PracticeType.CHALLENGE);
            }
        }
        if (result && submit.getPointStatus() == 0 && content.length() > 50) {
            logger.info("挑战训练加分:{}", id);
            // 未加分并且字数大于50(字母)
            PracticePlan practicePlan = practicePlanDao.loadPracticePlan(submit.getPlanId(),
                    submit.getChallengeId(), PracticePlan.CHALLENGE);
            if (practicePlan != null) {
                pointRepo.risePoint(submit.getPlanId(), ConfigUtils.getChallengeScore());
            }
            challengeSubmitDao.updatePointStatus(id);
        }

        return result;
    }
}