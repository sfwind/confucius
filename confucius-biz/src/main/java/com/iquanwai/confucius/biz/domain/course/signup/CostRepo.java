package com.iquanwai.confucius.biz.domain.course.signup;

/**
 * Created by justin on 16/10/13.
 */
public interface CostRepo {
    /**
     * 是否是课程免费用户
     * */
    boolean isWhite(Integer courseId, String openid);
    /**
     * 用户使用折扣后的实际金额
     * */
    double discount(Double price, String openid);

    /**
     * 是否有优惠券
     * */
    boolean hasCoupon(String openid);

    /**
     * 刷新缓存
     * */
    void reloadCache();
}
