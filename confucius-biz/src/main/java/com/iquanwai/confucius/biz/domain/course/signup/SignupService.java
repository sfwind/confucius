package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseOrder;
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
     * @param openid openid
     * @param courseId 课程id
     * @return {-1,0} 已报满，
     *         {-2,0} 没有设置课程，
     *         {-3,0} 已报名成功，
     *         {1,*} 预报名成功,返回班级id
     * */
    Pair<Integer, Integer> signupCheck(String openid, Integer courseId);

    Pair<Integer, String> riseMemberSignupCheckNoHold(String openId, Integer memberTypeId);

    /**
     * 课程报名，生成预付订单
     * @return 订单
     * */
    QuanwaiOrder signup(String openid, Integer courseId, Integer classId);

    Pair<Integer, String> riseMemberSignupCheck(String openId,Integer memberTypeId);

    /**
     * 报名rise, 不生成预付订单
     */
    Pair<Integer,QuanwaiOrder> signupRiseMember(String openid, Integer memberType,Integer couponId);


    /**
     * 该重载方法待删除
     * @return 订单
     */
    QuanwaiOrder signup(String openid, Integer courseId, Integer classId, String promoCode,Double discount);

    /**
     * 获取学员详情
     * */
    ClassMember classMember(String orderId);

    /**
     * 生成付款二维码
     * @return 报名二维码
     * */
    String payQRCode(String productId);

    /**
     * 根据班级id获取班级信息
     * */
    QuanwaiClass getCachedClass(Integer classId);

    /**
     * 根据课程id获取课程信息
     * */
    CourseIntroduction getCachedCourse(Integer courseId);
    /**
     * 根据订单号获取订单
     * */
    CourseOrder getOrder(String orderId);

    /**
     * 付款成功后入学
     * @param orderId 订单id
     * @return 返回学号
     * */
    String entry(String orderId);

    void riseMemberEntry(String orderId);

    /**
     * 是否免费
     * */
    boolean free(Integer courseId, String openid);

    /**
     * 未及时付款，去掉预报名抢占的名额
     * */
    void giveupSignup(String orderId);

    /**
     * 放弃rise的报名
     */
    void giveupRiseSignup(String orderId);

    /**
     * 发送课程报名成功消息
     * */
    void sendWelcomeMsg(Integer courseId, String openid, Integer classId);

    /**
     * 重新加载班级
     * */
    void reloadClass();

    String PAY_URL = "weixin://wxpay/bizpayurl?sign={sign}&appid={appid}&mch_id={mch_id}&product_id={product_id}&time_stamp={time_stamp}&nonce_str={nonce_str}";

    /**
     * 获取班级订单
     */
    CourseOrder getCourseOrder(String out_trade_no);

    List<QuanwaiOrder> getActiveOrders(String openId, Integer courseId);

    Map<Integer,Integer> getRemindingCount();

    Integer getRiseRemindingCount();

    void updatePromoCode(String orderId, String promoCode);

    /**
     * 获得圈外订单
     * @param orderId 订单id
     */
    QuanwaiOrder getQuanwaiOrder(String orderId);

    /**
     * 获得rise订单
     * @param orderId 订单id
     */
    RiseOrder getRiseOrder(String orderId);

    /**
     * 获取会员类型
     * @param memberType 会员类型Id
     * @return 会员类型
     */
    MemberType getMemberType(Integer memberType);

    /**
     * 获取用户可以使用的优惠券
     */
    List<Coupon> getCoupons(String openId);

    /**
     * 查询会员类型的支付信息
     */
    List<MemberType> getMemberTypesPayInfo();

    /**
     * 计算优惠券
     * @param memberTypeId 会员id
     * @param couponId 优惠券id
     * @return 打的折扣是多少
     */
    Double calculateCoupon(Integer memberTypeId, Integer couponId);

    RiseMember currentRiseMember(Integer profileId);
}
