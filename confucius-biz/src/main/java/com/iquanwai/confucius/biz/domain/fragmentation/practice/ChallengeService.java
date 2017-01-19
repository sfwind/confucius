package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.po.fragmentation.ChallengePractice;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengeSubmit;

/**
 * Created by nethunder on 2017/1/13.
 */
public interface ChallengeService {
    ChallengePractice loadChallengePractice(Integer id);

    ChallengePractice loadMineChallengePractice(Integer planId, Integer challengeId, String openId);

    Boolean submit(Integer id, String content);

    ChallengeSubmit loadSubmit(Integer id);
}
