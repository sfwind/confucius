package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.course.CouponDao;
import com.iquanwai.confucius.biz.dao.course.CourseFreeListDao;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.systematism.CourseFreeList;
import com.iquanwai.confucius.biz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/10/13.
 */
@Repository
public class CostRepoImpl implements CostRepo {
    @Autowired
    private CourseFreeListDao courseFreeListDao;
    @Autowired
    private CouponDao couponDao;

    private Logger logger = LoggerFactory.getLogger(getClass());
    //课程白名单
    private Map<Integer, List<String>> whiteList = Maps.newHashMap();
    //有优惠券的名单
    private List<String> couponList = Lists.newArrayList();

    @PostConstruct
    public void init(){
        List<CourseFreeList> courseFreeLists = courseFreeListDao.loadAll(CourseFreeList.class);
        whiteList.clear();
        for(CourseFreeList freeList:courseFreeLists){
            List<String> openids = whiteList.get(freeList.getCourseId());
            if(openids==null){
                openids = Lists.newArrayList();
                whiteList.put(freeList.getCourseId(), openids);
            }
            openids.add(freeList.getOpenid());
        }
        //初始化优惠券
        reloadCoupon();

        logger.info("init white list & coupon complete");
    }

    public boolean isWhite(Integer courseId, String openid) {
        List<String> classWhiteList = whiteList.get(courseId);
        //白名单中包含用户openid
        return classWhiteList!=null && classWhiteList.contains(openid);
    }

    public double discount(Double price, String openid, String orderId) {
        List<Coupon> coupons = couponDao.getCoupon(openid);
        Double remain = price;
        for(Coupon coupon:coupons){
            Double amount = coupon.getAmount();
            if(remain>amount){
                remain = CommonUtils.substract(remain, amount);
                couponDao.updateCoupon(coupon.getId(), Coupon.USING, orderId, amount);
            //余额为0时,仍然付0.01元
            }else if(remain.equals(amount)){
                remain = 0d;
                couponDao.updateCoupon(coupon.getId(), Coupon.USING, orderId, amount);
                break;
            }else{
                remain = 0d;
                couponDao.updateCoupon(coupon.getId(), Coupon.USING, orderId, CommonUtils.substract(amount, remain));
                break;
            }
        }
        reloadCoupon();
        return CommonUtils.substract(price,remain);
    }

    public boolean hasCoupon(String openid) {
        return couponList.contains(openid);
    }

    public void reloadCache() {
        init();
    }

    @Override
    public void updateCoupon(Integer status, String orderId) {
        couponDao.updateCouponByOrderId(status, orderId);
        reloadCoupon();
    }

    @Override
    public List<Coupon> getCoupons(String openId) {
        return couponDao.getCoupon(openId);
    }


    private void reloadCoupon(){
        List<Coupon> coupons  = couponDao.getUnusedCoupon();
        couponList.clear();
        coupons.stream().filter(coupon -> !couponList.contains(coupon.getOpenid()))
                .forEach(coupon -> couponList.add(coupon.getOpenid()));
    }
}
