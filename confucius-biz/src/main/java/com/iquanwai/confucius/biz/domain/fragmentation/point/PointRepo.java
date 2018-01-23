package com.iquanwai.confucius.biz.domain.fragmentation.point;

/**
 * Created by justin on 16/12/14.
 */
public interface PointRepo {
    /**
     * 提交小目标
     * @param planId 训练计划id
     * @param increment 积分增幅
     * */
    void risePoint(Integer planId, Integer increment);


    //小目标得分
    int CHALLENGE_PRACTICE_SCORE = 500;

    // 点赞得分
    int VOTE_SCORE = 2;

    /**
     * 给用户信息表加分
     */
    void riseCustomerPoint(Integer profileId, Integer increment);

    // 重新读取分数配置
    void reloadScore();
}
