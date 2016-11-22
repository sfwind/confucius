package com.iquanwai.confucius.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 16/9/10.
 */
@Data
public class CourseOrder {
    private int id;
    private String orderId; //订单id
    private String openid; //openid
    private Integer courseId; //课程id
    private String courseName; //课程名称
    private Integer classId; //班级id
    private Double price;   //实际金额 总金额-折扣金额
    private Double discount; //折扣金额
    private String prepayId; //预支付交易会话标识
    private Integer status;  //付费状态（0-待付费，1-已付费，2-付费取消，3-付费退款，4-付费失败）
    private Date paidTime;   //订单付款时间
    private Date createTime; //订单生成时间
    private String returnMsg; //微信返回信息
    private String transactionId; // 微信支付订单号

    public static final int UNDER_PAY = 0;
    public static final int PAID = 1;
    public static final int CANCELLED = 2;

}
