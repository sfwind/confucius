package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampOrder;
import com.iquanwai.confucius.biz.po.fragmentation.RiseCourseOrder;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseOrder;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.po.systematism.CourseIntroduction;
import com.iquanwai.confucius.biz.po.systematism.CourseOrder;
import com.iquanwai.confucius.biz.po.systematism.QuanwaiClass;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by justin on 16/8/29.
 */
public interface SignupService {


    Pair<Integer, String> risePurchaseCheck(Integer profileId, Integer memberType);


    Pair<Integer, String> riseMemberSignupCheck(Integer profileId, Integer memberTypeId);

    /**
     * 报名rise, 不生成预付订单
     */
    QuanwaiOrder signupRiseMember(Integer profileId, Integer memberTypeId, List<Integer> couponIdGroup);


    QuanwaiOrder signupMonthlyCamp(Integer profileId, Integer memberTypeId, Integer couponId);

    /**
     * 获取学员详情
     */
    ClassMember classMember(String orderId);


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

    String generateMemberId();

    void riseMemberEntry(String orderId);




    /**
     * 重新加载班级
     */
    void reloadClass();

    String PAY_URL = "weixin://wxpay/bizpayurl?sign={sign}&appid={appid}&mch_id={mch_id}&product_id={product_id}&time_stamp={time_stamp}&nonce_str={nonce_str}";


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
     *
     * @param orderId
     * @return
     */
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
    Double calculateMemberCoupon(Integer memberTypeId, List<Integer> couponId);


    /**
     * 计算小课训练营的优惠券
     */
    Double calculateCampCoupon(Integer profileId, Integer couponId);

    /**
     * 用户当前的会员
     */
    RiseMember currentRiseMember(Integer profileId);

    /**
     * 当月训练营
     */
    Integer loadCurrentCampMonth();

    /**
     * 小课售卖页面，跳转小课介绍页面 problemId
     */
    Integer loadHrefProblemId(Integer month);

    /**
     * 获取商学院数据
     *
     * @param profileId 用户id
     */
    BusinessSchool getSchoolInfoForPay(Integer profileId);
}
