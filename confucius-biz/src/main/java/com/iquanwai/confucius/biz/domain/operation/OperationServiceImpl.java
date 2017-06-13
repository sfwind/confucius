package com.iquanwai.confucius.biz.domain.operation;

import com.iquanwai.confucius.biz.dao.course.ClassMemberDao;
import com.iquanwai.confucius.biz.dao.course.CouponDao;
import com.iquanwai.confucius.biz.domain.course.signup.CostRepo;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class OperationServiceImpl implements OperationService {

    @Autowired
    private CouponDao couponDao;
    @Autowired
    private ClassMemberDao classMemberDao;
    @Autowired
    private CostRepo costRepo;

    @Override
    public Coupon alreadyGetDiscount(Integer profileId) {
        Coupon coupon = couponDao.getCouponByCategory(profileId, "ELITE_RISE_MEMBER");
        return coupon;
    }

    @Override
    public Integer getDiscountValue(Integer profileId) {
        Integer discount;
        // 高折扣:低折扣 = 2:8
        double random = Math.random() * 100;
        if (random >= 99) {
            discount = new Random().nextInt(100) + 400;
        } else if (random >= 80) {
            discount = new Random().nextInt(100) + 300;
        } else if (random >= 1) {
            discount = new Random().nextInt(100) + 200;
        } else {
            discount = new Random().nextInt(100) + 100;
        }
        // 用户获取成功优惠券之后，数据库进行记录
        Coupon coupon = new Coupon();
//        coupon.setOpenid(openId);
        coupon.setProfileId(profileId);
        coupon.setAmount(discount.doubleValue());
        coupon.setUsed(0);
        coupon.setExpiredDate(DateUtils.beforeDays(new Date(), 1));
        coupon.setCategory("ELITE_RISE_MEMBER");
        coupon.setDescription("精英奖学金");
        couponDao.insertGroupCategory(coupon);
        return discount;
    }

    /**
     * 获取该学员courseid为1235的课程数目(是否购买过圈外产品)
     */
    @Override
    public Integer getValidCourseCount(Integer profileId) {
        List<ClassMember> classMemberList = classMemberDao.classMember(profileId);
        Long validCourseCount = classMemberList.stream().filter(classMember -> "1235".contains(classMember.getCourseId().toString())).count();
        return validCourseCount.intValue();
    }

    /**
     * 更新优惠券的生效日期
     */
    @Override
    public Integer validDiscount(Integer profileId) {
        Coupon coupon = new Coupon();
//        coupon.setOpenid(openId);
        coupon.setProfileId(profileId);
        coupon.setCategory("ELITE_RISE_MEMBER");
        Integer result = couponDao.updateExpiredDate(coupon);
        costRepo.reloadCache();
        return result;
    }

}
