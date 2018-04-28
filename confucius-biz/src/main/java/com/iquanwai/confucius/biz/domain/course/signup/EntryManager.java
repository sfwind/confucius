package com.iquanwai.confucius.biz.domain.course.signup;

/**
 * Created by justin on 2018/4/26.
 */
public interface EntryManager {
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
     * 申请商学院付费后
     *
     * @param orderId 订单号
     */
    void payApplicationSuccess(String orderId);

    /**
     * 商学院购买成功处理
     */
    void payRiseSuccess(String orderId);
}
