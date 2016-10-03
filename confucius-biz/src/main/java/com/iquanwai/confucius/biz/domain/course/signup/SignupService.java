package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.po.ClassMember;
import com.iquanwai.confucius.biz.po.CourseIntroduction;
import com.iquanwai.confucius.biz.po.CourseOrder;
import com.iquanwai.confucius.biz.po.QuanwaiClass;
import org.apache.commons.lang3.tuple.Pair;

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
     *          其余情况返回剩余多少名额和班级id
     * */
    Pair<Integer, Integer> signupCheck(String openid, Integer courseId);

    /**
     * 课程报名，生成预付订单
     * @return 报名订单号
     * */
    String signup(String openid, Integer courseId, Integer classId);

    /**
     * 获取学员详情
     * */
    ClassMember classMember(String openid, Integer classId);

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
    CourseOrder getCourseOrder(String orderId);

    /**
     * 付款成功后入学
     * @return 返回学号
     * */
    String entry(Integer courseId, Integer classId, String openid);

    /**
     * 是否是白名单
     * */
    boolean isWhite(Integer courseId, String openid);

    /**
     * 未及时付款，去掉预报名抢占的名额
     * */
    void giveupSignup(String openid, String orderId);

    /**
     * 发送课程报名成功消息
     * */
    void sendWelcomeMsg(Integer courseId, String openid, Integer classId);

    String PAY_URL = "weixin://wxpay/bizpayurl?sign={sign}&appid={appid}&mch_id={mch_id}&product_id={product_id}&time_stamp={time_stamp}&nonce_str={nonce_str}";
}
