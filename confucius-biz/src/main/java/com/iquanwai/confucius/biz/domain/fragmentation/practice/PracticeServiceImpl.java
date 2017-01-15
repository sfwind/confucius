package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.dao.fragmentation.HomeworkVoteDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ChallengePracticeDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ChallengeSubmitDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.confucius.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.confucius.biz.po.HomeworkVote;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by justin on 16/12/11.
 */
@Service
public class PracticeServiceImpl implements PracticeService {
    @Autowired
    private ChallengePracticeDao challengePracticeDao;
    @Autowired
    private ChallengeSubmitDao challengeSubmitDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private PointRepo pointRepo;
    @Autowired
    private HomeworkVoteDao homeworkVoteDao;

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public ChallengePractice getChallengePracticeNoCreate(Integer challengeId, String openId, Integer planId) {
        Assert.notNull(openId, "openId不能为空");
        ChallengePractice challengePractice = challengePracticeDao.load(ChallengePractice.class, challengeId);

        ChallengeSubmit submit = challengeSubmitDao.load(challengeId, planId, openId);
        if (submit == null || submit.getContent() == null) {
            challengePractice.setSubmitted(false);
        } else {
            challengePractice.setSubmitted(true);
        }
        if (submit != null) {
            challengePractice.setContent(submit.getContent());
            challengePractice.setSubmitId(submit.getId());
            challengePractice.setSubmitUpdateTime(submit.getUpdateTime());
        }
        challengePractice.setPlanId(planId);
        return challengePractice;
    }


    @Override
    public ChallengePractice getChallengePractice(Integer id, String openid, Integer planId) {
        Assert.notNull(openid, "openid不能为空");
        ChallengePractice challengePractice = challengePracticeDao.load(ChallengePractice.class, id);

        ChallengeSubmit submit = challengeSubmitDao.load(id, planId, openid);
        if (submit == null || submit.getContent() == null) {
            challengePractice.setSubmitted(false);
        } else {
            challengePractice.setSubmitted(true);
        }
        //生成挑战训练提交记录
        if (submit == null) {
            submit = new ChallengeSubmit();
            submit.setOpenid(openid);
            submit.setPlanId(planId);
            submit.setChallengeId(id);
            int submitId = -1;
            submitId = challengeSubmitDao.insert(submit);
            submit.setId(submitId);
            submit.setUpdateTime(new Date());
        }
        challengePractice.setContent(submit.getContent());
        challengePractice.setSubmitId(submit.getId());
        challengePractice.setSubmitUpdateTime(submit.getUpdateTime());
        challengePractice.setPlanId(planId);
        return challengePractice;
    }

    @Override
    public List<ChallengePractice> getChallengePracticesByProblem(Integer problem) {
        return challengePracticeDao.loadPractice(problem);
    }


    @Override
    public List<ChallengeSubmit> getChallengeSubmitList(Integer challengeId) {
        return challengeSubmitDao.loadList(challengeId);
    }

    @Override
    public ChallengePractice getChallenge(Integer id) {
        return challengePracticeDao.load(ChallengePractice.class, id);
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
                pointRepo.risePoint(submit.getPlanId(), PointRepo.CHALLENGE_PRACTICE_SCORE);
            }
            challengeSubmitDao.updatePointStatus(id);
        }

        return result;
    }


    @Override
    public List<ChallengePractice> loadPractice(int problemId) {
        return challengePracticeDao.loadPractice(problemId);
    }

    @Override
    public ChallengeSubmit loadChallengeSubmit(Integer submitId) {
        return challengeSubmitDao.load(ChallengeSubmit.class, submitId);
    }


    @Override
    public Integer loadHomeworkVotesCount(Integer type, Integer referencedId) {
        return homeworkVoteDao.votedCount(type, referencedId);
    }


    @Override
    public void vote(Integer type, Integer referencedId, String openId) {
        HomeworkVote vote = homeworkVoteDao.loadVoteRecord(type, referencedId, openId);
        Pair<Integer, String> pair = new MutablePair<>();
        if (vote == null) {
            homeworkVoteDao.vote(type, referencedId, openId);
        } else {
            homeworkVoteDao.reVote(vote.getId());
        }
    }

    @Override
    public Pair<Integer, String> disVote(Integer type, Integer referencedId, String openId) {
        HomeworkVote vote = homeworkVoteDao.loadVoteRecord(type, referencedId, openId);
        if (vote == null) {
            // 没有
            return new MutablePair<Integer, String>(0, "没有您的点赞记录");
        } else {
            homeworkVoteDao.disVote(vote.getId());
            return new MutablePair<Integer, String>(1, "success");
        }
    }

    @Override
    public HomeworkVote loadVoteRecord(Integer type, Integer referId, String openId) {
        return homeworkVoteDao.loadVoteRecord(type, referId, openId);
    }


}
