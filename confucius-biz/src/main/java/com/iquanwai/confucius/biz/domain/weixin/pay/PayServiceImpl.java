package com.iquanwai.confucius.biz.domain.weixin.pay;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.iquanwai.confucius.biz.dao.wx.QuanwaiOrderDao;
import com.iquanwai.confucius.biz.domain.course.signup.CostRepo;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.util.*;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/9/14.
 */
@Service
public class PayServiceImpl implements PayService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private QuanwaiOrderDao quanwaiOrderDao;
    @Autowired
    private CostRepo costRepo;
    @Autowired
    private SignupService signupService;
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private MessageService messageService;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    private RabbitMQPublisher paySuccessPublisher;

    private RabbitMQPublisher freshLoginUserPublisher;

    private static final String WEIXIN = "NATIVE";
    private static final String JSAPI = "JSAPI";
    private static final String FAIL = "FAIL";

    private static final String PAY_CALLBACK_PATH = "/wx/pay/result/callback";
    private static final String RISE_MEMBER_PAY_CALLBACK_PATH = "/wx/pay/result/risemember/callback";
    private static final String RISE_COURSE_PAY_CALLBACK_PATH = "/wx/pay/result/risecourse/callback";
    private static final String RISE_CAMP_PAY_CALLBACK_PATH = "/wx/pay/result/risecamp/callback";


    @PostConstruct
    public void init() {
        // 初始化发送mq
        paySuccessPublisher = rabbitMQFactory.initFanoutPublisher(RISE_PAY_SUCCESS_TOPIC);
        freshLoginUserPublisher = rabbitMQFactory.initFanoutPublisher(LOGIN_USER_RELOAD_TOPIC);
    }

    @Override
    public String unifiedOrder(String orderId) {
        Assert.notNull(orderId, "订单号不能为空");
        QuanwaiOrder courseOrder = quanwaiOrderDao.loadOrder(orderId);
        if (courseOrder == null) {
            logger.error("order id {} not existed", orderId);
            return "";
        }

        UnifiedOrder unifiedOrder = buildOrder(courseOrder);

        String response = restfulHelper.postXML(UNIFIED_ORDER_URL, XMLHelper.createXML(unifiedOrder));
        UnifiedOrderReply reply = XMLHelper.parseXml(UnifiedOrderReply.class, response);
        if (reply != null) {
            String prepay_id = reply.getPrepay_id();
            if (prepay_id != null) {
                quanwaiOrderDao.updatePrepayId(prepay_id, orderId);
                return prepay_id;
            }
            if (reply.getErr_code_des() != null) {
                logger.error("response is------\n" + response);
                logger.error(reply.getErr_code_des() + ", orderId=" + orderId);
                if (!ignoreCode(reply.getErr_code())) {
                    quanwaiOrderDao.payError(reply.getErr_code_des(), orderId);
                }
            }

        }
        return "";
    }

    private boolean ignoreCode(String err_code) {
        return SYSTEM_ERROR.equals(err_code) || DUP_PAID.equals(err_code) || ORDER_CLOSE.equals(err_code);
    }

    @Override
    public OrderCallbackReply callbackReply(String result, String errMsg, String prepayId) {
        Assert.notNull(result, "支付结果不能为空");
        Assert.notNull(errMsg, "描述不能为空");
        OrderCallbackReply orderCallbackReply = new OrderCallbackReply();

        Map<String, String> map = Maps.newHashMap();
        map.put("result_code", result);
        map.put("err_code_des", errMsg);
        map.put("prepay_id", prepayId);
        String return_code = "SUCCESS";
        map.put("return_code", return_code);
        String appid = ConfigUtils.getAppid();
        map.put("appid", appid);
        String mch_id = ConfigUtils.getMch_id();
        map.put("mch_id", mch_id);
        String nonce_str = CommonUtils.randomString(16);
        map.put("nonce_str", nonce_str);

        String sign = CommonUtils.sign(map);
        orderCallbackReply.setSign(sign);
        orderCallbackReply.setAppid(appid);
        orderCallbackReply.setErr_code_des(errMsg);
        orderCallbackReply.setMch_id(mch_id);
        orderCallbackReply.setNonce_str(nonce_str);
        orderCallbackReply.setPrepay_id(prepayId);
        orderCallbackReply.setResult_code(result);
        orderCallbackReply.setReturn_code(return_code);

        return orderCallbackReply;
    }

    @Override
    public void handlePayResult(PayCallback payCallback) {
        Assert.notNull(payCallback, "支付结果不能为空");
        String orderId = payCallback.getOut_trade_no();
        if (payCallback.getErr_code_des() != null) {
            logger.error(payCallback.getErr_code_des() + ", orderId=" + orderId);
            if (!ignoreCode(payCallback.getErr_code())) {
                quanwaiOrderDao.payError(payCallback.getErr_code_des(), orderId);
            }
            return;
        }

        String transactionId = payCallback.getTransaction_id();
        String paidTimeStr = payCallback.getTime_end();
        Date paidTime = DateUtils.parseStringToDate3(paidTimeStr);
        quanwaiOrderDao.paySuccess(paidTime, transactionId, orderId);
    }

    @Override
    public void handlePayResult(String orderId, Boolean isFree) {
        Assert.notNull(orderId, "支付结果不能为空");
        Date paidTime = new Date();
        quanwaiOrderDao.paySuccess(paidTime, null, orderId);
    }

    @Override
    public void paySuccess(String orderId) {
        QuanwaiOrder quanwaiOrder = quanwaiOrderDao.loadOrder(orderId);
        Assert.notNull(quanwaiOrder, "订单不存在，OrderId：" + orderId);

        if (QuanwaiOrder.FRAG_MEMBER.equals(quanwaiOrder.getGoodsType())) {
            // 商品是rise会员
            signupService.riseMemberEntry(quanwaiOrder.getOrderId());
        } else if (QuanwaiOrder.FRAG_CAMP.equals(quanwaiOrder.getGoodsType())) {
            // 购买训练营
            signupService.payMonthlyCampSuccess(orderId);
        }
        refreshStatus(quanwaiOrder, orderId);
    }

    // 购买会员
    @Override
    public void payMemberSuccess(String orderId) {
        QuanwaiOrder quanwaiOrder = quanwaiOrderDao.loadOrder(orderId);
        Assert.notNull(quanwaiOrder, "订单不存在，OrderId:" + orderId);
        Assert.isTrue(QuanwaiOrder.FRAG_MEMBER.equals(quanwaiOrder.getGoodsType()));
        // 商品是rise会员
        signupService.riseMemberEntry(quanwaiOrder.getOrderId());
        refreshStatus(quanwaiOrder, orderId);
    }

    private void refreshStatus(QuanwaiOrder quanwaiOrder, String orderId) {
        // 刷新会员状态
        try {
            freshLoginUserPublisher.publish(quanwaiOrder.getOpenid());
        } catch (ConnectException e) {
            logger.error("发送会员信息更新mq失败", e);
        }
        // 更新优惠券使用状态
        if (quanwaiOrder.getDiscount() != 0.0) {
            logger.info("{}使用优惠券", quanwaiOrder.getOpenid());
            costRepo.updateCoupon(Coupon.USED, orderId);
        }
        // 发送mq消息
        try {
            logger.info("发送支付成功message:{}", quanwaiOrder);
            paySuccessPublisher.publish(quanwaiOrder);
        } catch (ConnectException e) {
            logger.error("发送支付成功mq失败", e);
            messageService.sendAlarm("报名模块出错", "发送支付成功mq失败", "高", "订单id:" + orderId, e.getLocalizedMessage());
        }
    }

    @Override
    public Map<String, String> buildH5PayParam(String orderId, String ip, String openId) {
        String prepayId = unifiedOrder(orderId, ip);
        Assert.notNull(prepayId, "预付款Id不能为空");
        Map<String, String> map = Maps.newHashMap();
        map.put("appId", ConfigUtils.getAppid());
        map.put("timeStamp", String.valueOf(DateUtils.currentTimestamp()));
        map.put("nonceStr", CommonUtils.randomString(32));
        map.put("package", "prepay_id=" + prepayId);
        map.put("signType", "MD5");
        String sign = CommonUtils.sign(map);
        map.put("paySign", sign);
        logger.info("校验参数：{}", map);
        return map;
    }

    private String unifiedOrder(String orderId, String ip) {
        Assert.notNull(orderId, "订单号不能为空");
        Assert.notNull(ip, "IP不能为空");

        QuanwaiOrder quanwaiOrder = quanwaiOrderDao.loadOrder(orderId);
        if (quanwaiOrder == null) {
            logger.error("order id {} not existed", orderId);
            return "";
        }

        UnifiedOrder unifiedOrder = buildJSApiOrder(quanwaiOrder, ip);

        String response = restfulHelper.postXML(UNIFIED_ORDER_URL, XMLHelper.createXML(unifiedOrder));
        UnifiedOrderReply reply = XMLHelper.parseXml(UnifiedOrderReply.class, response);
        if (reply != null) {
            String prepay_id = reply.getPrepay_id();
            if (prepay_id != null) {
                quanwaiOrderDao.updatePrepayId(prepay_id, orderId);
                return prepay_id;
            }
            if (reply.getErr_code_des() != null) {
                logger.error("response is------\n" + response);
                logger.error(reply.getErr_code_des() + ", orderId=" + orderId);
                if (!ignoreCode(reply.getErr_code())) {
                    quanwaiOrderDao.payError(reply.getErr_code_des(), orderId);
                }
            }

        }
        return "";
    }

    /**
     * 根据预先生成的 order 订单数据，生成对微信的请求 url，xml 格式
     */
    private UnifiedOrder buildJSApiOrder(QuanwaiOrder quanwaiOrder, String ip) {
        UnifiedOrder unifiedOrder = new UnifiedOrder();
        Map<String, String> map = Maps.newHashMap();
        String appid = ConfigUtils.getAppid();
        map.put("appid", appid);
        String mch_id = ConfigUtils.getMch_id();
        map.put("mch_id", mch_id);
        String nonce_str = CommonUtils.randomString(16);
        map.put("nonce_str", nonce_str);
        String body = GOODS_BODY;
        map.put("body", body);
        String openid = quanwaiOrder.getOpenid();
        map.put("openid", openid);

        String notify_url = null;
        if (QuanwaiOrder.FRAG_MEMBER.equals(quanwaiOrder.getGoodsType())) {
            notify_url = ConfigUtils.adapterDomainName() + RISE_MEMBER_PAY_CALLBACK_PATH;
        } else if (QuanwaiOrder.FRAG_CAMP.equals(quanwaiOrder.getGoodsType())) {
            notify_url = ConfigUtils.adapterDomainName() + RISE_CAMP_PAY_CALLBACK_PATH;
        }

        Assert.notNull(notify_url, "回调地址不能为空");
        map.put("notify_url", notify_url);
        String out_trade_no = quanwaiOrder.getOrderId();
        map.put("out_trade_no", out_trade_no);
        String trade_type = JSAPI;
        map.put("trade_type", trade_type);
        map.put("spbill_create_ip", ip);
        String time_start = DateUtils.parseDateToString3(new Date());
        map.put("time_start", time_start);
        String time_expire = DateUtils.parseDateToString3(
                DateUtils.afterMinutes(new Date(), ConfigUtils.getBillOpenMinute()));
        map.put("time_expire", time_expire);
        Integer total_fee = (int) (quanwaiOrder.getPrice() * 100);
        map.put("total_fee", total_fee.toString());

        String detail = buildOrderDetail(quanwaiOrder, total_fee);
        map.put("detail", detail);

        String sign = CommonUtils.sign(map);

        unifiedOrder.setAppid(appid);
        unifiedOrder.setMch_id(mch_id);
        unifiedOrder.setNonce_str(nonce_str);
        unifiedOrder.setBody(body);
        unifiedOrder.setOpenid(openid);
        unifiedOrder.setNotify_url(notify_url);
        unifiedOrder.setOut_trade_no(out_trade_no);
        unifiedOrder.setTrade_type(trade_type);
        unifiedOrder.setSpbill_create_ip(ip);
        unifiedOrder.setTime_start(time_start);
        unifiedOrder.setTime_expire(time_expire);
        unifiedOrder.setTotal_fee(total_fee);
        //加CDATA标签
        unifiedOrder.setDetail(XMLHelper.appendCDATA(detail));
        unifiedOrder.setSign(sign);

        return unifiedOrder;
    }

    private UnifiedOrder buildOrder(QuanwaiOrder quanwaiOrder) {
        UnifiedOrder unifiedOrder = new UnifiedOrder();
        Map<String, String> map = Maps.newHashMap();
        String appid = ConfigUtils.getAppid();
        map.put("appid", appid);
        String mch_id = ConfigUtils.getMch_id();
        map.put("mch_id", mch_id);
        String nonce_str = CommonUtils.randomString(16);
        map.put("nonce_str", nonce_str);
        String body = GOODS_BODY;
        map.put("body", body);
        String openid = quanwaiOrder.getOpenid();
        map.put("openid", openid);
        String notify_url = ConfigUtils.adapterDomainName() + PAY_CALLBACK_PATH;
        map.put("notify_url", notify_url);
        String out_trade_no = quanwaiOrder.getOrderId();
        map.put("out_trade_no", out_trade_no);
        String trade_type = WEIXIN;
        map.put("trade_type", trade_type);
        String spbill_create_ip = ConfigUtils.getExternalIP();
        map.put("spbill_create_ip", spbill_create_ip);
        String time_start = DateUtils.parseDateToString3(new Date());
        map.put("time_start", time_start);
        String time_expire = DateUtils.parseDateToString3(
                DateUtils.afterMinutes(new Date(), ConfigUtils.getBillOpenMinute()));
        map.put("time_expire", time_expire);
        Integer total_fee = (int) (quanwaiOrder.getPrice() * 100);
        map.put("total_fee", total_fee.toString());

        String detail = buildOrderDetail(quanwaiOrder, total_fee);
        map.put("detail", detail);

        String sign = CommonUtils.sign(map);

        unifiedOrder.setAppid(appid);
        unifiedOrder.setMch_id(mch_id);
        unifiedOrder.setNonce_str(nonce_str);
        unifiedOrder.setBody(body);
        unifiedOrder.setOpenid(openid);
        unifiedOrder.setNotify_url(notify_url);
        unifiedOrder.setOut_trade_no(out_trade_no);
        unifiedOrder.setTrade_type(trade_type);
        unifiedOrder.setSpbill_create_ip(spbill_create_ip);
        unifiedOrder.setTime_start(time_start);
        unifiedOrder.setTime_expire(time_expire);
        unifiedOrder.setTotal_fee(total_fee);
        //加CDATA标签
        unifiedOrder.setDetail(XMLHelper.appendCDATA(detail));
        unifiedOrder.setSign(sign);

        return unifiedOrder;
    }

    private String buildOrderDetail(QuanwaiOrder quanwaiOrder, Integer total_fee) {
        OrderDetail orderDetail = new OrderDetail();
        List<GoodsDetail> goodsDetailList = Lists.newArrayList();
        orderDetail.setGoodsDetail(goodsDetailList);
        GoodsDetail goodsDetail = new GoodsDetail();
        goodsDetail.setPrice(total_fee);
        goodsDetail.setGoods_id(quanwaiOrder.getGoodsId());
        goodsDetail.setGoods_name(quanwaiOrder.getGoodsName());
        goodsDetail.setGoods_num(1);
        goodsDetailList.add(goodsDetail);
        return new Gson().toJson(orderDetail);
    }

    @Override
    public void refund(String orderId, Double fee) {
        QuanwaiOrder quanwaiOrder = quanwaiOrderDao.loadOrder(orderId);
        RefundOrder refundOrder = buildRefundOrder(quanwaiOrder, fee);
        String response = restfulHelper.sslPostXml(REFUND_ORDER_URL, XMLHelper.createXML(refundOrder));

        RefundOrderReply reply = XMLHelper.parseXml(RefundOrderReply.class, response);
        if (reply != null) {
            if (FAIL.equals(reply.getReturn_code()) || FAIL.equals(reply.getResult_code())) {
                logger.error("response is------\n" + response);
                messageService.sendAlarm("退款出错", "退款接口调用失败",
                        "高", "订单id:" + orderId, "msg:" + reply.getReturn_msg() + ", error:" + reply.getErr_code_des());
            } else {
                quanwaiOrderDao.refundOrder(orderId, fee, refundOrder.getOut_refund_no());
            }

        }
    }

    private RefundOrder buildRefundOrder(QuanwaiOrder quanwaiOrder, Double fee) {
        RefundOrder refundOrder = new RefundOrder();
        Map<String, String> map = Maps.newHashMap();
        String appid = ConfigUtils.getAppid();
        map.put("appid", appid);
        String mch_id = ConfigUtils.getMch_id();
        map.put("mch_id", mch_id);
        String nonce_str = CommonUtils.randomString(16);
        map.put("nonce_str", nonce_str);
        String out_trade_no = quanwaiOrder.getOrderId();
        map.put("out_trade_no", out_trade_no);
        String out_refund_no = CommonUtils.randomString(16);
        map.put("out_refund_no", out_refund_no);
        Integer total_fee = (int) (quanwaiOrder.getPrice() * 100);
        map.put("total_fee", total_fee.toString());
        Integer refund_fee = (int) (fee * 100);
        map.put("refund_fee", refund_fee.toString());

        String sign = CommonUtils.sign(map);

        refundOrder.setAppid(appid);
        refundOrder.setMch_id(mch_id);
        refundOrder.setNonce_str(nonce_str);
        refundOrder.setOut_trade_no(out_trade_no);
        refundOrder.setTotal_fee(total_fee);
        refundOrder.setRefund_fee(refund_fee);
        refundOrder.setOut_refund_no(out_refund_no);
        refundOrder.setSign(sign);

        return refundOrder;
    }
}
