package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.dao.course.CouponDao;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.util.CommonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 16/10/13.
 */
@Repository
public class CostRepoImpl implements CostRepo {
    @Autowired
    private CouponDao couponDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public double discount(Double price, Integer profileId, String orderId) {
        List<Coupon> coupons = couponDao.loadCoupons(profileId);
        Double remain = price;
        for (Coupon coupon : coupons) {
            Double amount = coupon.getAmount();
            if (remain > amount) {
                remain = CommonUtils.substract(remain, amount);
                couponDao.updateCoupon(coupon.getId(), Coupon.USING, orderId, amount);
                //余额为0时,仍然付0.01元
            } else if (remain.equals(amount)) {
                remain = 0d;
                couponDao.updateCoupon(coupon.getId(), Coupon.USING, orderId, amount);
                break;
            } else {
                remain = 0d;
                couponDao.updateCoupon(coupon.getId(), Coupon.USING, orderId, CommonUtils.substract(amount, remain));
                break;
            }
        }
        return CommonUtils.substract(price, remain);
    }

    @Override
    public double discount(Double price, String orderId, Coupon coupon) {
        Double remain = price;
        Double amount = coupon.getAmount();
        if (remain > amount) {
            remain = CommonUtils.substract(remain, amount);
            couponDao.updateCoupon(coupon.getId(), Coupon.USING, orderId, amount);
        } else {
            remain = 0D;
            couponDao.updateCoupon(coupon.getId(), Coupon.USING, orderId, CommonUtils.substract(amount, remain));
        }
        return CommonUtils.substract(price, remain);
    }

    @Override
    public boolean checkCouponValidation(Integer profileId, Integer couponId) {
        if(couponId != null) {
            Coupon coupon = couponDao.load(Coupon.class, couponId);
            return coupon.getProfileId().equals(profileId);
        } else {
            return true;
        }
    }

    public boolean hasCoupon(Integer profileId) {
        List<Coupon> coupons = couponDao.getCoupon(profileId);
        return CollectionUtils.isNotEmpty(coupons);
    }

    @Override
    public void updateCoupon(Integer status, String orderId) {
        couponDao.updateCouponByOrderId(status, orderId);
    }

    @Override
    public List<Coupon> getCoupons(Integer profileId) {
        return couponDao.loadCoupons(profileId);
    }

    @Override
    public Coupon getCoupon(Integer id) {
        return couponDao.load(Coupon.class, id);
    }

}
