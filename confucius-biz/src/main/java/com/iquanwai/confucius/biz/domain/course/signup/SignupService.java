package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.po.systematism.CourseIntroduction;
import com.iquanwai.confucius.biz.po.systematism.CourseOrder;
import com.iquanwai.confucius.biz.po.systematism.QuanwaiClass;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/8/29.
 */
public interface SignupService {
    /**
     * 计算课程是否有剩余名额
     *
     * @param profileId 用户id
     * @param courseId  课程id
     * @return {-1,0} 已报满，
     * {-2,0} 没有设置课程，
     * {1,*} 预报名成功,返回班级id
     */
    Pair<Integer, Integer> signupCheck(Integer profileId, Integer courseId);

    /**
     * 检查该用户是否可以购买该小课
     * @param profileId 用户id
     * @param problemId 小课id
     */
    Pair<Integer, String> riseCourseSignupCheck(Integer profileId, Integer problemId);

    Pair<Integer, String> risePurchaseCheck(Integer profileId, Integer memberType);

    /**
     * 检查是否在报名中
     * @param profileId 用户id
     */
    Pair<Integer, String> riseMemberSignupCheckNoHold(Integer profileId, Integer memberTypeId);

    /**
     * 课程报名，生成预付订单
     *
     * @return 订单
     */
    QuanwaiOrder signupCourse(String openid, Integer profileId, Integer courseId, Integer classId);

    Pair<Integer, String> riseMemberSignupCheck(Integer profileId, Integer memberTypeId);

    /**
     * 报名rise, 不生成预付订单
     */
    QuanwaiOrder signupRiseMember(Integer profileId, Integer memberType, Integer couponId);

    QuanwaiOrder signupRiseCourse(Integer profileId, Integer problemId, Integer couponId);

    QuanwaiOrder signupMonthlyCamp(Integer profileId, Integer couponId);

    /**
     * 获取学员详情
     */
    ClassMember classMember(String orderId);

    /**
     * 生成付款二维码
     *
     * @return 报名二维码
     */
    String payQRCode(String productId);

    /**
     * 根据班级id获取班级信息
     */
    QuanwaiClass getCachedClass(Integer classId);

    /**
     * 根据课程id获取课程信息
     */
    CourseIntroduction getCachedCourse(Integer courseId);

    /**
     * 根据订单号获取订单
     */
    CourseOrder getOrder(String orderId);

    /**
     * 付款成功后入学
     *
     * @param orderId 订单id
     * @return 返回学号
     */
    String entry(String orderId);

    void riseCourseEntry(String orderId);

    /**
     * 购买完训练营小课后续操作
     * 1、更新 Profile RiseMember 值
     * 2、RiseMemberClass 新增数据记录
     * 3、更新 RiseMember 表旧数据为过期状态，并新增一条当前购买类型数据记录
     * 4、送优惠券
     * 5、发送 mq 通知 platon 强制开启小课
     * 6、发送购买成功信息，开课信息（可以合并）
     */
    void payMonthlyCampSuccess(String orderId);

    MonthlyCampOrder getMonthlyCampOrder(String orderId);

    void riseMemberEntry(String orderId);

    /**
     * 未及时付款，去掉预报名抢占的名额
     */
    void giveupSignup(String orderId);

    void giveupRiseCourseSignup(String orderId);

    /**
     * 放弃rise的报名
     */
    void giveupRiseSignup(String orderId);

    /**
     * 重新加载班级
     */
    void reloadClass();

    String PAY_URL = "weixin://wxpay/bizpayurl?sign={sign}&appid={appid}&mch_id={mch_id}&product_id={product_id}&time_stamp={time_stamp}&nonce_str={nonce_str}";

    Map<Integer, Integer> getRemindingCount();

    Integer getRiseRemindingCount();

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

    RiseCourseOrder getRiseCourse(String orderId);

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
     * 计算优惠券
     *
     * @param memberTypeId 会员id
     * @param couponId     优惠券id
     * @return 打的折扣是多少
     */
    Double calculateCoupon(Integer memberTypeId, Integer couponId);

    /**
     * 计算小课单卖多少钱
     * @param problemId 小课id
     * @param couponId 优惠券id
     */
    Double calculateCourseCoupon(Integer problemId,Integer profileId, Integer couponId);

    Double calculateCampCoupon(Integer profileId, Integer couponId);

    RiseMember currentRiseMember(Integer profileId);
}
