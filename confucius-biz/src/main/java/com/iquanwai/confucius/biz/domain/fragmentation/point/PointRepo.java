package com.iquanwai.confucius.biz.domain.fragmentation.point;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by justin on 16/12/14.
 */
public interface PointRepo {
    /**
     * 提交挑战训练
     * @param planId 训练计划id
     * @param increment 积分增幅
     * */
    void risePoint(Integer planId, Integer increment);


    //挑战训练得分
    int CHALLENGE_PRACTICE_SCORE = 500;

    void reloadScore();
}
