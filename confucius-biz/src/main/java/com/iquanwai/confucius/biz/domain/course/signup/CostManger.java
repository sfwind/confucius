package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.po.Coupon;

import java.util.List;

/**
 * Created by justin on 16/10/13.
 */
public interface CostManger {
    /** 计算折扣价 */
    double discount(Double price, String orderId, Coupon coupon);

    /** 计算折扣价 */
    double discount(Double price, String orderId, List<Coupon> coupon);

    /**
     * 判断优惠券是否有效
     */
    boolean checkCouponValidation(Integer profileId, Integer couponId);

    /**
     * 是否有优惠券
     */
    boolean hasCoupon(Integer profileId);

    /**
     * 更新优惠券状态
     */
    void updateCoupon(Integer status, String orderId);

    /**
     * 获取所有的优惠券
     */
    List<Coupon> getCoupons(Integer profileId);

    /**
     * 获取某张优惠券
     */
    Coupon getCoupon(Integer id);
}
