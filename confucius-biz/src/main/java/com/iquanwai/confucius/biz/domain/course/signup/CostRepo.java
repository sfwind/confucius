package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.po.Coupon;

import java.util.List;

/**
 * Created by justin on 16/10/13.
 */
public interface CostRepo {
    double discount(Double price, String orderId, Coupon coupon);

    double discount(Double price, String orderId, List<Coupon> coupon);

    boolean checkCouponValidation(Integer profileId, Integer couponId);

    /**
     * 是否有优惠券
     */
    boolean hasCoupon(Integer profileId);

    /**
     * 更新优惠券状态
     */
    void updateCoupon(Integer status, String orderId);

    List<Coupon> getCoupons(Integer profileId);

    Coupon getCoupon(Integer id);
}
