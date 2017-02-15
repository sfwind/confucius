package com.iquanwai.confucius.biz.domain.course.operational;

import com.iquanwai.confucius.biz.dao.course.CouponDao;
import com.iquanwai.confucius.biz.dao.operational.PromoCodeDao;
import com.iquanwai.confucius.biz.dao.operational.PromoCodeUsageDao;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.PromoCode;
import com.iquanwai.confucius.biz.po.PromoCodeUsage;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/2/14.
 */
@Service
public class PromoCodeServiceImpl implements PromoCodeService{
    @Autowired
    private PromoCodeDao promoCodeDao;
    @Autowired
    private PromoCodeUsageDao promoCodeUsageDao;
    @Autowired
    private CouponDao couponDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public PromoCode getPromoCode(String openid) {
        //TODO:需要不断切换活动码,目前只支持一个running的活动
        PromoCode promoCode = promoCodeDao.queryPromoCodeByOwner(openid, ActivityFactory.ACTIVITY_CAREER_COURSE_PACKAGE);
        if (isValid(promoCode)) {
            return promoCode;
        }

        return null;
    }

    private boolean isValid(PromoCode promoCode) {
        //优惠码存在且没过期
        if(promoCode!=null && promoCode.getExpiredDate()!=null && promoCode.getExpiredDate().after(new Date())){
            return true;
        }
        return false;
    }

    @Override
    public Double discount(String code) {
        PromoCode promoCode = promoCodeDao.queryPromoCode(code, ActivityFactory.ACTIVITY_CAREER_COURSE_PACKAGE);
        if (isValid(promoCode)) {
            return promoCode.getDiscount();
        }
        return -1.0;
    }

    @Override
    public void usePromoCode(String openid, String code) {
        Activity activity = ActivityFactory.getActivity(ActivityFactory.ACTIVITY_CAREER_COURSE_PACKAGE);
        if(activity==null){
            logger.error("活动 {} 不存在", ActivityFactory.ACTIVITY_CAREER_COURSE_PACKAGE);
            return;
        }
        logger.info("使用优惠券{}", code);
        careerActivity(activity, openid, code);

    }

    @Override
    public List<PromoCode> getPromoCodes(String activityCode) {
        return promoCodeDao.queryPromoCodeByActivityCode(activityCode);
    }

    private void careerActivity(Activity activity, String openid, String code){
        PromoCode promoCode = promoCodeDao.queryPromoCode(code, activity.getName());
        if(promoCode==null){
            logger.error("优惠码{}不存在", code);
            return;
        }
        if(promoCode.getUseCount()<activity.getPromoCodeUsageLimit()){
            synchronized (this){
                promoCode = promoCodeDao.queryPromoCode(code, activity.getName());
                if(promoCode.getUseCount()<activity.getPromoCodeUsageLimit()){
                    //插入优惠券
                    Coupon coupon = new Coupon();
                    coupon.setOpenid(promoCode.getOwner());
                    coupon.setAmount(promoCode.getDiscount());
                    coupon.setUsed(0);
                    //过期日期是结束日期+1
                    coupon.setExpiredDate(DateUtils.afterDays(activity.getEndDate(),1));
                    couponDao.insert(coupon);
                }
            }
        }
        //优惠码使用次数+1
        promoCodeDao.incrementPromoCodeUsage(code, activity.getName());
        PromoCodeUsage promoCodeUsage = new PromoCodeUsage();
        promoCodeUsage.setPromoCodeId(promoCode.getId());
        promoCodeUsage.setUser(openid);
        promoCodeUsageDao.insert(promoCodeUsage);
    }
}
