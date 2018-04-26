package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.common.coupon.CouponDao;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.util.CommonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by justin on 16/10/13.
 */
@Service
public class CostMangerImpl implements CostManger {
    @Autowired
    private CouponDao couponDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public double discount(Double price, String orderId, Coupon coupon) {
        return discount(price, orderId, Lists.newArrayList(coupon));
    }

    @Override
    public double discount(Double price, String orderId, List<Coupon> coupons) {
        Double remain = price;
        Double amount = coupons.stream().mapToDouble(Coupon::getAmount).sum();
        if (remain > amount) {
            remain = CommonUtils.substract(remain, amount);
            coupons.forEach(coupon -> couponDao.updateCoupon(coupon.getId(), orderId, amount));
        } else {
            remain = 0D;
            for (Coupon coupon : coupons) {
                couponDao.updateCoupon(coupon.getId(), orderId, CommonUtils.substract(amount, remain));
            }
        }
        return CommonUtils.substract(price, remain);
    }

    @Override
    public boolean checkCouponValidation(Integer profileId, Integer couponId) {
        if (couponId != null) {
            Coupon coupon = couponDao.load(Coupon.class, couponId);
            return coupon.getProfileId().equals(profileId);
        } else {
            return true;
        }
    }

    @Override
    public boolean hasCoupon(Integer profileId) {
        List<Coupon> coupons = couponDao.loadCoupons(profileId);
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
