package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.course.CouponDao;
import com.iquanwai.confucius.biz.dao.course.CourseFreeListDao;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.CourseFreeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Date;
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
        if(classWhiteList==null||!classWhiteList.contains(openid)){
            return false;
        }
        return true;
    }

    public double discount(Double price, String openid) {
        List<Coupon> coupons = couponDao.getCoupon(openid);
        List<Integer> usedCoupon = Lists.newArrayList();
        double remain = price;
        for(Coupon coupon:coupons){
            double amount = coupon.getAmount();
            if(remain>amount){
                remain = remain-amount;
            //折扣券大于课程费用
            }else{
                remain = 0.0;
                usedCoupon.add(coupon.getId());
//                double newAmount = amount-remain;
//                Coupon newCoupon = new Coupon();
//                newCoupon.setAmount(newAmount);
//                newCoupon.setExpiredDate(coupon.getExpiredDate());
//                newCoupon.setOpenid(openid);
//                newCoupon.setUsed(0);
//                couponDao.insert(coupon);
                break;
            }
            usedCoupon.add(coupon.getId());
        }
        couponDao.updateCoupon(usedCoupon, 2);
        reloadCoupon();
        return price-remain;
    }

    public boolean hasCoupon(String openid) {
        return couponList.contains(openid);
    }

    public void reloadCache() {
        init();
    }

    private void reloadCoupon(){
        List<Coupon> coupons  = couponDao.loadAll(Coupon.class);
        couponList.clear();
        for(Coupon coupon:coupons){
            //只包含未使用的优惠券
            if(coupon.getUsed()==0){
                if(coupon.getExpiredDate()!=null){
                    //已过期的不算
                    if(coupon.getExpiredDate().before(new Date())){
                        continue;
                    }
                }
                if(!couponList.contains(coupon.getOpenid())){
                    couponList.add(coupon.getOpenid());
                }
            }
        }
    }
}
