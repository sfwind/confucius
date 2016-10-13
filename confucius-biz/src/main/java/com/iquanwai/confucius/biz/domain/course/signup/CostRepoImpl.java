package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.course.CouponDao;
import com.iquanwai.confucius.biz.dao.course.CourseFreeListDao;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.CourseFreeList;
import com.iquanwai.confucius.biz.util.DateUtils;
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
    //优惠券
    private Map<String, List<Coupon>> couponList = Maps.newHashMap();

    @PostConstruct
    public void init(){
        whiteList.clear();
        couponList.clear();
        List<CourseFreeList> courseFreeLists = courseFreeListDao.loadAll(CourseFreeList.class);
        for(CourseFreeList freeList:courseFreeLists){
            List<String> openids = whiteList.get(freeList.getCourseId());
            if(openids==null){
                openids = Lists.newArrayList();
                whiteList.put(freeList.getCourseId(), openids);
            }
            openids.add(freeList.getOpenid());
        }
        List<Coupon> coupons  = couponDao.loadAll(Coupon.class);
        for(Coupon coupon:coupons){
            if(coupon.getUsed()==0){
                List<Coupon> list = couponList.get(coupon.getOpenid());
                if(list==null){
                    list = Lists.newArrayList();
                    couponList.put(coupon.getOpenid(), list);
                }
                list.add(coupon);
            }
        }
        logger.info("init white list & coupon complete");
    }

    public boolean free(Integer courseId, String openid) {
        List<String> classWhiteList = whiteList.get(courseId);
        if(classWhiteList==null||!classWhiteList.contains(openid)){
            return false;
        }
        return true;
    }

    public double discount(Double price, String openid) {
//        List<Coupon> coupons = couponDao.getCoupon(openid);
//        List<Integer> usedCoupon = Lists.newArrayList();
//        double remain = price;
//        for(Coupon coupon:coupons){
//            double amount = coupon.getAmount();
//            if(remain>=amount){
//                remain = remain-amount;
//                if(remain==0.0){
//                    break;
//                }
//            }else{
//                double newAmount = amount-remain;
//                Coupon newCoupon = new Coupon();
//                newCoupon.setAmount(newAmount);
//                newCoupon.setExpiredDate(defaultExpiredDate());
//                newCoupon.setOpenid(openid);
//                newCoupon.setUsed(0);
//                couponDao.insert(coupon);
//                break;
//            }
//            usedCoupon.add(coupon.getId());
//        }
//        couponDao.updateCoupon(usedCoupon, 2);
//        return price-remain;

        return price;
    }

    public void reloadCache() {
        init();
    }

    private Date defaultExpiredDate() {
        return DateUtils.afterYears(new Date(), 100);
    }
}
