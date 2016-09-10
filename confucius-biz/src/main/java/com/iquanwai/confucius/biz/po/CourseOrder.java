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
    private Integer classId; //班级id
    private Integer price;   //实际金额 总金额-折扣金额
    private Integer discount; //折扣金额
    private String prepayId; //预支付交易会话标识
    private Integer status;  //付费状态（0-待付费，1-已付费，2-付费取消，3-付费退款，4-付费失败）
    private Date paidTime;   //订单付款时间
    private Date createTime; //订单生成时间
    private String returnMsg; //微信返回信息

}
