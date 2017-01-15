package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.po.fragmentation.ChallengePractice;

/**
 * Created by nethunder on 2017/1/13.
 */
public interface ChallengeService {
    ChallengePractice loadChallengePractice(Integer id);

    ChallengePractice loadMineChallengePractice(Integer planId, Integer challengeId, String openId);
}
