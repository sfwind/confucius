package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.fragmentation.BusinessSchoolApplicationOrder;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampConfig;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampOrder;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseOrder;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by justin on 16/8/29.
 */
public interface SignupService {

    /**
     * 商品支付资格校验
     */
    Pair<Integer, String> risePurchaseCheck(Integer profileId, Integer memberType);

    /**
     * 报名商学院, 不生成预付订单
     */
    QuanwaiOrder signUpRiseMember(Integer profileId, Integer memberTypeId, List<Integer> couponId, Integer payType);

    /**
     * 报名专项课, 不生成预付订单
     */
    QuanwaiOrder signUpMonthlyCamp(Integer profileId, Integer memberTypeId, Integer couponId, Integer payType);

    /**
     * 申请商学院, 不生成预付订单
     */
    QuanwaiOrder signupBusinessSchoolApplication(Integer profileId, Integer memberTypeId, Integer couponId, Integer payType);

    /**
     * 购买完专项课后续操作
     * 1、更新 Profile RiseMember 值
     * 2、RiseMemberClass 新增数据记录
     * 3、更新 RiseMember 表旧数据为过期状态，并新增一条当前购买类型数据记录
     * 4、送优惠券
     * 5、发送 mq 通知 platon 强制开启课程
     * 6、发送购买成功信息，开课信息（可以合并）
     */
    void payMonthlyCampSuccess(String orderId);

    /**
     * 获取专项课订单
     */
    MonthlyCampOrder getMonthlyCampOrder(String orderId);

    /**
     * 生成学号
     */
    String generateMemberId(Integer year, Integer month, Integer identityType);

    /**
     * 商学院购买成功处理
     */
    void payRiseSuccess(String orderId);

    /**
     * 获得圈外订单
     *
     * @param orderId 订单id
     */
    QuanwaiOrder getQuanwaiOrder(String orderId);

    /**
     * 获得rise订单
     *
     * @param orderId 订单id
     */
    RiseOrder getRiseOrder(String orderId);

    /**
     * 获取会员类型
     *
     * @param memberType 会员类型Id
     * @return 会员类型
     */
    MemberType getMemberType(Integer memberType);

    /**
     * 获取用户可以使用的优惠券
     */
    List<Coupon> getCoupons(Integer profileId);

    /**
     * 查询会员类型的支付信息
     */
    List<MemberType> getMemberTypesPayInfo();

    /**
     * 查询会员类型的支付信息
     */
    List<MemberType> getMemberTypesPayInfo(Integer profileId);

    /**
     * 计算优惠券
     *
     * @param memberTypeId 会员id
     * @param couponId     优惠券id
     * @return 打的折扣是多少
     */
    Double calculateMemberCoupon(Integer profileId, Integer memberTypeId, List<Integer> couponId);


    /**
     * 用户当前的会员
     */
    RiseMember currentRiseMember(Integer profileId);

    /**
     * 当月专项课
     */
    Integer loadCurrentCampMonth(MonthlyCampConfig monthlyCampConfig);

    /**
     * 课程售卖页面，跳转课程介绍页面 problemId
     */
    Integer loadHrefProblemId(Integer profileId, Integer month);

    /**
     * 获取商学院数据
     *
     * @param profileId 用户id
     */
    BusinessSchool getSchoolInfoForPay(Integer profileId);

    /**
     * 获取用户当前会员信息
     *
     * @param profileId 用户id
     */
    RiseMember getCurrentRiseMemberStatus(Integer profileId);

    /**
     * 获取当前专项课信息
     */
    RiseMember getCurrentMonthlyCampStatus(Integer profileId);

    /**
     * 获取用户所有的用户信息
     */
    List<RiseMember> loadPersonalAllRiseMembers(Integer profileId);


    /**
     * 申请商学院付费后
     */
    void payApplicationSuccess(String orderId);

    /**
     * 获取商学院申请订单
     */
    BusinessSchoolApplicationOrder getBusinessSchoolOrder(String orderId);

    /**
     * 获取商学院申请订单
     */
    boolean isAppliedBefore(Integer profileId);

    /**
     * 根据商品类型和售价智能选择优惠券
     */
    List<Coupon> autoChooseCoupon(String goodsType, Double fee, List<Coupon> coupons);
}
