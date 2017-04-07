package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.po.Coupon;

import java.util.List;

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
    double discount(Double price, String openid, String orderId);

    /**
     * 是否有优惠券
     * */
    boolean hasCoupon(String openid);

    /**
     * 刷新缓存
     * */
    void reloadCache();

    /**
     * 更新优惠券状态
     * */
    void updateCoupon(Integer status, String orderId);

    List<Coupon> getCoupons(String openId);

}
