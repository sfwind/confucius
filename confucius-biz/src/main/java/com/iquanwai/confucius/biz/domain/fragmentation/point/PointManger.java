package com.iquanwai.confucius.biz.domain.fragmentation.point;

/**
 * Created by justin on 16/12/14.
 */
public interface PointManger {
    /**
     * 打分
     * @param planId 训练计划id
     * @param increment 积分增幅
     * */
    void risePoint(Integer planId, Integer increment);

    /**
     * 给用户信息表加分
     */
    void riseCustomerPoint(Integer profileId, Integer increment);
}
