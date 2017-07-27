package com.iquanwai.confucius.biz.domain.weixin.pay;

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


    /**
     * 订单付款成功
     * @param orderId 订单id
     * */
    void paySuccess(String orderId);

    /**
     * rise支付成功
     * @param orderId 订单id
     */
    void risePaySuccess(String orderId);

    /**
     * 定期关闭过期订单
     * */
    void closeOrder();

    /**
     * 关闭订单
     * @param orderId 订单号
     * */
    void closeOrder(String orderId);

    /**
     * 校验支付签名
     * @param prepayId 预付订单号
     */
    Map<String,String> buildH5PayParam(String prepayId,String ip,String openId);


    String UNIFIED_ORDER_URL ="https://api.mch.weixin.qq.com/pay/unifiedorder";

    String CLOSE_ORDER_URL ="https://api.mch.weixin.qq.com/pay/closeorder";

    String GOODS_BODY = "圈外-线上课程";

    String ERROR_CODE = "FAIL";
    String SUCCESS_CODE = "SUCCESS";

    String DUP_PAID = "ORDERPAID";
    String ORDER_CLOSE = "ORDERCLOSED";
    String SYSTEM_ERROR = "SYSTEMERROR";
    String RISE_PAY_SUCCESS_TOPIC = "rise_pay_success_topic";
    String LOGIN_USER_RELOAD_TOPIC ="login_user_reload";


}
