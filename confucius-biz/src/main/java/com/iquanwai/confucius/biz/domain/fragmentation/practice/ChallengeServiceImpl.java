package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.dao.fragmentation.ChallengePracticeDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ChallengeSubmitDao;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengePractice;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengeSubmit;
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
}
