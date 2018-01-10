package com.iquanwai.confucius.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 17/1/19.
 */
@Data
public class QuanwaiOrder {
    private int id;
    private String orderId; //订单id
    private String openid; //openid
    private Double price;   //实际金额 总金额-折扣金额
    private Double discount; //折扣金额
    private String prepayId; //预支付交易会话标识
    private Integer status;  //付费状态（0-待付费，1-已付费，2-付费取消，3-付费退款，4-付费失败）
    private Date paidTime;   //订单付款时间
    private Date createTime; //订单生成时间
    private String returnMsg; //微信返回信息
    private String transactionId; // 微信支付订单号
    private String goodsId; //商品id
    private String goodsType; //商品类型
    private String goodsName; //商品名称
    private Double total; //非db字段,订单的原始金额,不计折扣金额
    private Date refundTime; //退款时间

    /** 商学院 */
    public static final String FRAG_MEMBER = "fragment_member";
    /** 训练营 */
    public static final String FRAG_CAMP = "fragment_camp";
    /** 商学院申请 */
    public static final String BS_APPLICATION = "bs_application";

    public static final int UNDER_PAY = 0;
    public static final int PAID = 1;
    public static final int CANCELLED = 2;
    public static final int REFUND = 3;
    public static final int REFUND_FAILED = 4;
}
