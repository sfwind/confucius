package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.course.HomeworkVoteDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ChallengePracticeDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ChallengeSubmitDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.confucius.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.confucius.biz.po.HomeworkVote;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.util.WebUtils;

import javax.jws.WebResult;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private PointRepo pointRepo;
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private HomeworkVoteDao homeworkVoteDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static String shortUrlService = "http://tinyurl.com/api-create.php?url=";

    private final static String submitUrlPrefix = "/fragment/c?id=";

    @Override
    public ChallengePractice getChallengePractice(Integer id, String openId) {
        ChallengePractice challengePractice = challengePracticeDao.load(ChallengePractice.class, id);
        List<ChallengeSubmit> submits = challengeSubmitDao.load(id, openId);
        Optional<ChallengeSubmit> optional = submits.stream().filter(item -> item.getContent() != null).findFirst();
        challengePractice.setSubmitted(optional.isPresent());
        if (challengePractice.getSubmitted()) {
            // 如果用户提交的话，则将提交的放上来
            challengePractice.setSubmitUrl(optional.get().getSubmitUrl());
            challengePractice.setSubmitId(optional.get().getId());
            challengePractice.setContent(optional.get().getContent());
            challengePractice.setPcurl(optional.get().getShortUrl());
        }
        return challengePractice;
    }

    @Override
    public ChallengePractice getChallengePracticeNoCreate(Integer id, String openId, Integer planId) {
        Assert.notNull(openId, "openId不能为空");
        ChallengePractice challengePractice = challengePracticeDao.load(ChallengePractice.class, id);

        ChallengeSubmit submit = challengeSubmitDao.load(id, planId, openId);
        if (submit == null || submit.getContent() == null) {
            challengePractice.setSubmitted(false);
        } else {
            challengePractice.setSubmitted(true);
        }
        if (submit != null) {
            if (submit.getSubmitUrl() != null) {
                challengePractice.setPcurl(submit.getShortUrl());
            }
            challengePractice.setContent(submit.getContent());
            challengePractice.setSubmitUrl(submit.getSubmitUrl());
            challengePractice.setSubmitId(submit.getId());
            challengePractice.setSubmitUpdateTime(submit.getUpdateTime());
        }
        return challengePractice;
    }

    @Override
    public ChallengeSubmit getChallengeSubmit(Integer id, String openId, Integer planId) {
        return challengeSubmitDao.load(id, planId, openId);
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
        } else {
            challengePractice.setContent(submit.getContent());
            challengePractice.setSubmitId(submit.getId());
            challengePractice.setSubmitUpdateTime(submit.getUpdateTime());
        }
        return challengePractice;
    }

    @Override
    public List<ChallengePractice> getChallengePracticesByProblem(Integer problem) {
        return challengePracticeDao.loadPractice(problem);
    }


    @Override
    public ChallengePractice getDoingChallengePractice(String openId) {
        List<ProblemList> problemLists = problemService.loadProblems(openId);
        Integer doingProblem = null;
        for (ProblemList problemList : problemLists) {
            if (problemList.getStatus() == 1) {
                doingProblem = problemList.getProblemId();
                break;
            }
        }
        if (doingProblem == null && problemLists.size() != 0) {
            doingProblem = problemLists.get(0).getProblemId();
        }
        return this.getChallengePractice(doingProblem, openId);
    }

    @Override
    public List<ChallengeSubmit> getChallengeSubmitList(Integer challengeId){
        return challengeSubmitDao.loadList(challengeId);
    }



    @Override
    public ChallengePractice getChallengePractice(String code) {
        String submitUrl = submitUrlPrefix + code;
        ChallengeSubmit challengeSubmit = challengeSubmitDao.load(submitUrl);
        if (challengeSubmit == null) {
            logger.error("code {} is not existed", submitUrl);
            return null;
        }
        return getChallengePractice(challengeSubmit.getChallengeId(), challengeSubmit.getOpenid(),
                challengeSubmit.getPlanId());
    }

    @Override
    public ChallengePractice getChallenge(Integer id) {
        return challengePracticeDao.load(ChallengePractice.class, id);
    }

    private String generateShortUrl(String url) {
        String requestUrl = shortUrlService;
        try {
            requestUrl = requestUrl + URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException ignored) {

        }
        String shortUrl = restfulHelper.getPlain(requestUrl);
        if (shortUrl.startsWith("http")) {
            return shortUrl;
        } else {
            return url;
        }
    }

    @Override
    public  Boolean submit(Integer id,String content){
        return challengeSubmitDao.answer(id,content);
    }
    @Override
    public Boolean submit(String code, String content) {
        String submitUrl = submitUrlPrefix + code;
        ChallengeSubmit challengeSubmit = challengeSubmitDao.load(submitUrl);
        if (challengeSubmit == null) {
            logger.error("code {} is not existed", submitUrl);
            return false;
        }
        boolean result = challengeSubmitDao.answer(challengeSubmit.getId(), content);
//        if (result) {
//            PracticePlan practicePlan = practicePlanDao.loadPracticePlan(challengeSubmit.getPlanId(),
//                    challengeSubmit.getChallengeId(), PracticePlan.CHALLENGE);
//            if (practicePlan != null && practicePlan.getStatus() == 0) {
//                practicePlanDao.complete(practicePlan.getId());
//                improvementPlanDao.updateComplete(challengeSubmit.getPlanId());
//                pointRepo.risePoint(challengeSubmit.getPlanId(), PointRepo.CHALLENGE_PRACTICE_SCORE);
//            }
//        }
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
