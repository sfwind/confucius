package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.po.systematism.HomeworkVote;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengePractice;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengeSubmit;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by justin on 16/12/11.
 */
public interface PracticeService {


    ChallengePractice getChallengePracticeNoCreate(Integer challengeId, String openId, Integer planId);



    /**
     * 获取挑战训练
     * @param id 挑战训练id
     * @param openid 学员id
     * @param planId 训练计划id
     * */
    ChallengePractice getChallengePractice(Integer id, String openid, Integer planId);




    List<ChallengeSubmit> getChallengeSubmitList(Integer challengeId);

    ChallengePractice getChallenge(Integer id);




    ChallengeSubmit loadChallengeSubmit(Integer challengeId);

    /**
     * 查询点赞数
     * @param type 1：挑战任务，2：体系化大作业
     * @param referencedId 被依赖的id
     * @return 点赞数
     */
    Integer loadHomeworkVotesCount(Integer type,Integer referencedId);

    /**
     * 点赞
     * @param type 1：挑战任务，2：体系化大作业
     * @param referencedId 被依赖的id
     * @param openId 点赞的人
     */
    void vote(Integer type, Integer referencedId, String openId);

    /**
     * 取消点赞
     */
    Pair<Integer,String> disVote(Integer type, Integer referencedId, String openId);

    /**
     * 查询点赞记录
     */
    HomeworkVote loadVoteRecord(Integer type, Integer referId, String openId);
}
