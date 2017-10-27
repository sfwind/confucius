package com.iquanwai.confucius.biz.domain.weixin.pay;

import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampConfig;

import java.util.Map;

/**
 * Created by justin on 16/9/14.
 */
public interface PayService {

    /**
     * 调用微信统一下单接口
     * @return 返回PrepayId
     * */
    String unifiedOrder(String orderId);

    /**
     * 生成微信支付回调返回
     * */
    OrderCallbackReply callbackReply(String result, String errMsg, String prepayId);

    /**
     * 处理支付结果
     * */
    void handlePayResult(PayCallback payCallback);

    /**
     * 非微信支付处理
     * @param orderId 订单id
     * @param isFree 是否免费
     */
    void handlePayResult(String orderId,Boolean isFree);


    void risePaySuccess(String orderId, MonthlyCampConfig monthlyCampConfig);

    // 购买会员
    void payMemberSuccess(String orderId, MonthlyCampConfig monthlyCampConfig);

    /**
     * 校验支付签名
     * @param prepayId 预付订单号
     */
    Map<String,String> buildH5PayParam(String prepayId,String ip,String openId);

    /**
     * 退款
     * @param orderId 订单号
     * @param fee 退款金额
     * */
    void refund(String orderId, Double fee);

    String UNIFIED_ORDER_URL ="https://api.mch.weixin.qq.com/pay/unifiedorder";
    String REFUND_ORDER_URL ="https://api.mch.weixin.qq.com/secapi/pay/refund";

    String GOODS_BODY = "圈外-线上课程";

    String ERROR_CODE = "FAIL";
    String SUCCESS_CODE = "SUCCESS";

    String DUP_PAID = "ORDERPAID";
    String ORDER_CLOSE = "ORDERCLOSED";
    String SYSTEM_ERROR = "SYSTEMERROR";
    String RISE_PAY_SUCCESS_TOPIC = "rise_pay_success_topic";
    String LOGIN_USER_RELOAD_TOPIC ="login_user_reload";


}
