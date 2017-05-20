package com.iquanwai.confucius.biz.domain.operation;

import com.iquanwai.confucius.biz.dao.course.ClassMemberDao;
import com.iquanwai.confucius.biz.dao.course.CouponDao;
import com.iquanwai.confucius.biz.dao.course.CourseDao;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.po.systematism.Course;
import com.iquanwai.confucius.biz.po.systematism.Page;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class OperationServiceImpl implements OperationService {

    @Autowired
    private CouponDao couponDao;
    @Autowired
    private ClassMemberDao classMemberDao;
    @Autowired
    private CourseDao courseDao;

    @Override
    public Boolean alreadyGetDiscount(String openId) {
        List<Coupon> couponList = couponDao.getCouponByCategory(openId, "ELITE_RISE_MEMBER");
        Boolean alreadyGet = couponList.size() > 0 ? true : false;
        return alreadyGet;
    }

    @Override
    public Integer getDiscountValue(String openId) {
        Integer discount;
        List<ClassMember> classMemberList = classMemberDao.loadByOpenId(openId);
        List<Course> courseList = courseDao.loadAll(Course.class);
        List<Integer> formalIdList = courseList.stream().filter(course -> course.getType() != 3).map(course -> course.getId())
                .collect(Collectors.toList());
        Long validCount = classMemberList.stream().filter(classMember -> formalIdList.contains(classMember.getCourseId())).count();
        Long graduateCount = classMemberList.stream().filter(classMember -> classMember.getGraduate()).count();
        if (validCount == 0) {
            return 0;
        }
        if(graduateCount == 0) {
            discount = new Random().nextInt(200) + 1;
        } else {
            // 高折扣:低折扣 = 2:8
            double random = Math.random() * 10;
            if (random <= 2) {
                discount = new Random().nextInt(100) + 301;
            } else {
                discount = new Random().nextInt(100) + 201;
            }
            // 用户获取成功优惠券之后，数据库进行记录
            Coupon coupon = new Coupon();
            //OpenId, Amount, Used, ExpiredDate, Category, Description
            coupon.setOpenid(openId);
            coupon.setAmount(discount.doubleValue());
            coupon.setUsed(0);
            coupon.setExpiredDate(ConfigUtils.getDiscountExpiredDate());
            coupon.setCategory("ELITE_RISE_MEMBER");
            coupon.setDescription("精英奖学金");
            couponDao.insertGroupCategory(coupon);
        }
        System.out.println("discount = " + discount);
        return discount;
    }

}
