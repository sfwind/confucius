package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.po.Course;
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
     *         {-3,*} 有课程的最大人数设置不正确，
     *          其余情况返回剩余多少名额和班级id
     * */
    Pair<Integer, Integer> signupCheck(String openid, Integer courseId);

    /**
     * 课程报名
     * @return 报名订单号
     * */
    String signup(String openid, Integer courseId, Integer classId);

    /**
     * 生成付款二维码
     * @return 报名二维码
     * */
    String qrcode(String productId);

    /**
     * 根据班级id获取班级信息
     * */
    QuanwaiClass getCachedClass(Integer classId);

    /**
     * 根据课程id获取课程信息
     * */
    Course getCachedCourse(Integer courseId);
    /**
     * 根据订单号获取订单
     * */
    CourseOrder getCourseOrder(String orderId);

    /**
     * 付款成功后入学
     * @return 返回学号
     * */
    String entry(Integer classId, String openid);

    /**
     * 是否是白名单
     * */
    boolean isWhite(Integer courseId, String openid);

    String PAY_URL = "weixin：//wxpay/bizpayurl?sign=XXXXX&appid={app_id}&mch_id={mch_id}&product_id={product_id}&time_stamp={time_stamp}&nonce_str={nonce_str}";
}
