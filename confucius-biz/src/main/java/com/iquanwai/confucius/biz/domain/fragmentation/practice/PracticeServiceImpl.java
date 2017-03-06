package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.dao.fragmentation.ApplicationSubmitDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ChallengePracticeDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ChallengeSubmitDao;
import com.iquanwai.confucius.biz.dao.fragmentation.CommentDao;
import com.iquanwai.confucius.biz.dao.fragmentation.FragmentAnalysisDataDao;
import com.iquanwai.confucius.biz.dao.fragmentation.HomeworkVoteDao;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengePractice;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengeSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.Comment;
import com.iquanwai.confucius.biz.po.fragmentation.FragmentDailyData;
import com.iquanwai.confucius.biz.po.systematism.HomeworkVote;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

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
    private HomeworkVoteDao homeworkVoteDao;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private FragmentAnalysisDataDao fragmentAnalysisDataDao;

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
            // 生成浏览记录
            fragmentAnalysisDataDao.insertArticleViewInfo(Constants.ViewInfo.Module.CHALLENGE, submitId);
        }
        challengePractice.setContent(submit.getContent());
        challengePractice.setSubmitId(submit.getId());
        challengePractice.setSubmitUpdateTime(submit.getUpdateTime());
        challengePractice.setPlanId(planId);
        return challengePractice;
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
            homeworkVoteDao.vote(type, referencedId, openId, Constants.Device.PC);
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

    @Override
    public List<Comment> loadComments(Integer type, Integer referId, Page page) {
        return commentDao.loadComments(type,referId,page);
    }

    @Override
    public Integer commentCount(Integer moduleId,Integer referId){
        return commentDao.commentCount(moduleId,referId);
    }

    @Override
    public Pair<Boolean,String> comment(Integer moduleId, Integer referId, String openId, String content){
        if(moduleId== Constants.CommentModule.CHALLENGE){
            ChallengeSubmit load = challengeSubmitDao.load(ChallengeSubmit.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}",moduleId,referId,content);
                return new MutablePair<>(false,"没有该文章");
            }
        } else {
            ApplicationSubmit load = applicationSubmitDao.load(ApplicationSubmit.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}",moduleId,referId,content);
                return new MutablePair<>(false,"没有该文章");
            }
        }
        Comment comment = new Comment();
        comment.setModuleId(moduleId);
        comment.setReferencedId(referId);
        comment.setType(Constants.CommentType.STUDENT);
        comment.setContent(content);
        comment.setCommentOpenId(openId);
        comment.setDevice(Constants.Device.PC);
        commentDao.insert(comment);
        return new MutablePair<>(true,"评论成功");
    }

    @Override
    public void fragmentDailyPracticeData() {
        logger.info("search fragment daily practice data");
        FragmentDailyData dailyData = fragmentAnalysisDataDao.getDailyData();
        fragmentAnalysisDataDao.insertDailyData(dailyData);
    }

    @Override
    public Integer riseArticleViewCount(Integer module, Integer id, Integer type) {
        return fragmentAnalysisDataDao.riseArticleViewCount(module, id, type);
    }


}
