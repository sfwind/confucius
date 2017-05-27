package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.po.fragmentation.ChallengePractice;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengeSubmit;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by nethunder on 2017/1/13.
 */
public interface ChallengeService {

    ChallengePractice loadMineChallengePractice(Integer planId, Integer challengeId, String openId,boolean create);

    Pair<Integer,Integer> submit(Integer id, String content);

    ChallengeSubmit loadSubmit(Integer id);

    /**
     * 获取小目标
     * @param id 小目标id
     * @param openid 学员id
     * @param planId 训练计划id
     * */
    ChallengePractice getChallengePractice(Integer id, String openid, Integer planId,boolean create);


    ChallengePractice getChallenge(Integer id);
}
