package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.po.QuanwaiOrder;
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

    /**
     * 课程报名，生成预付订单
     * @return 订单
     * */
    QuanwaiOrder signup(String openid, Integer courseId, Integer classId);

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

    /**
     * 是否免费
     * */
    boolean free(Integer courseId, String openid);

    /**
     * 未及时付款，去掉预报名抢占的名额
     * */
    void giveupSignup(String orderId);

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

    void updatePromoCode(String orderId, String promoCode);

    QuanwaiOrder getQuanwaiOrder(String orderId);
}
