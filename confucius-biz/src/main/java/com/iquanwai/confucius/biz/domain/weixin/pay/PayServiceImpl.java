package com.iquanwai.confucius.biz.domain.weixin.pay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.iquanwai.confucius.biz.dao.wx.QuanwaiOrderDao;
import com.iquanwai.confucius.biz.domain.course.signup.CostRepo;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.exception.RefundException;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import com.iquanwai.confucius.biz.util.XMLHelper;
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
    @Autowired
    private AccountService accountService;

    private RabbitMQPublisher paySuccessPublisher;

    private RabbitMQPublisher freshLoginUserPublisher;

    private static final String WEIXIN = "NATIVE";
    private static final String JSAPI = "JSAPI";
    private static final String FAIL = "FAIL";

    private static final String PAY_CALLBACK_PATH = "/wx/pay/result/callback";
    private static final String RISE_MEMBER_PAY_CALLBACK_PATH = "/wx/pay/result/risemember/callback";
    private static final String RISE_CAMP_PAY_CALLBACK_PATH = "/wx/pay/result/risecamp/callback";
    private static final String BS_APPLICATION_PAY_CALLBACK_PATH = "/wx/pay/result/application/callback";

    private static final String ALIPAY_CALLBACK_PATH = "/ali/pay/callback/notify";
    private static final String ALIPAY_RETURN_PATH = "/pay/alipay/return";


    @PostConstruct
    public void init() {
        // 初始化发送mq
        paySuccessPublisher = rabbitMQFactory.initFanoutPublisher(RISE_PAY_SUCCESS_TOPIC);
        freshLoginUserPublisher = rabbitMQFactory.initFanoutPublisher(LOGIN_USER_RELOAD_TOPIC);
    }

    @Override
    public String unifiedOrder(String orderId) {
        Assert.notNull(orderId, "订单号不能为空");
        QuanwaiOrder quanwaiOrder = quanwaiOrderDao.loadOrder(orderId);
        if (quanwaiOrder == null) {
            logger.error("order id {} not existed", orderId);
            return "";
        }

        UnifiedOrder unifiedOrder = buildOrder(quanwaiOrder);

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
        QuanwaiOrder order = quanwaiOrderDao.loadOrder(orderId);
        if (payCallback.getErr_code_des() != null) {
            logger.error(payCallback.getErr_code_des() + ", orderId=" + orderId);
            if (!ignoreCode(payCallback.getErr_code())) {
                quanwaiOrderDao.payError(payCallback.getErr_code_des(), orderId);
            }
            return;
        }

        String transactionId = payCallback.getTransaction_id();
        String paidTimeStr = payCallback.getTime_end();
        Date paidTime = null;
        if (order.getPayType() == QuanwaiOrder.PAY_ALI) {
            paidTime = DateUtils.parseStringToDateTime(paidTimeStr);
        } else if (order.getPayType() == QuanwaiOrder.PAY_WECHAT) {
            paidTime = DateUtils.parseStringToDate3(paidTimeStr);
        }
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
            signupService.payRiseSuccess(quanwaiOrder.getOrderId());
        } else if (QuanwaiOrder.BS_APPLICATION.equals(quanwaiOrder.getGoodsType())) {
            // 购买商学院申请
            signupService.payApplicationSuccess(orderId);
        } else if (QuanwaiOrder.FRAG_CAMP.equals(quanwaiOrder.getGoodsType())) {
            // 购买专项课
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
        signupService.payRiseSuccess(quanwaiOrder.getOrderId());
        refreshStatus(quanwaiOrder, orderId);
    }

    private void refreshStatus(QuanwaiOrder quanwaiOrder, String orderId) {
        Profile profile = accountService.getProfile(quanwaiOrder.getProfileId());

        // 刷新会员状态
        try {
            freshLoginUserPublisher.publish(profile.getUnionid());
        } catch (ConnectException e) {
            logger.error("发送会员信息更新mq失败", e);
        }
        // 更新优惠券使用状态
        if (quanwaiOrder.getDiscount() != 0.0) {
            logger.info("{}使用优惠券", profile.getOpenid());
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
        Profile profile = accountService.getProfile(quanwaiOrder.getProfileId());
        Assert.notNull(profile, "用户不能为空");
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
        String openid = profile.getOpenid();
        map.put("openid", openid);

        String notify_url = null;
        if (QuanwaiOrder.FRAG_MEMBER.equals(quanwaiOrder.getGoodsType())) {
            notify_url = ConfigUtils.adapterDomainName() + RISE_MEMBER_PAY_CALLBACK_PATH;
        } else if (QuanwaiOrder.FRAG_CAMP.equals(quanwaiOrder.getGoodsType())) {
            notify_url = ConfigUtils.adapterDomainName() + RISE_CAMP_PAY_CALLBACK_PATH;
        } else if (QuanwaiOrder.BS_APPLICATION.equals(quanwaiOrder.getGoodsType())) {
            notify_url = ConfigUtils.adapterDomainName() + BS_APPLICATION_PAY_CALLBACK_PATH;
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
        Profile profile = accountService.getProfile(quanwaiOrder.getProfileId());
        Assert.notNull(profile, "用户不能为空");
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
        String openid = profile.getOpenid();
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

    private String buildOrderDetail(QuanwaiOrder quanwaiOrder, Integer totalFee) {
        OrderDetail orderDetail = new OrderDetail();
        List<GoodsDetail> goodsDetailList = Lists.newArrayList();
        orderDetail.setGoodsDetail(goodsDetailList);
        GoodsDetail goodsDetail = new GoodsDetail();
        goodsDetail.setPrice(totalFee);
        goodsDetail.setGoods_id(quanwaiOrder.getGoodsId());
        goodsDetail.setGoods_name(quanwaiOrder.getGoodsName());
        goodsDetail.setGoods_num(1);
        goodsDetailList.add(goodsDetail);
        return new Gson().toJson(orderDetail);
    }

    @Override
    public void refund(String orderId, Double fee) {
        QuanwaiOrder quanwaiOrder = quanwaiOrderDao.loadOrder(orderId);
        if (quanwaiOrder.getPayType() == QuanwaiOrder.PAY_WECHAT) {
            this.refundWechatPay(quanwaiOrder, fee);
        } else if (quanwaiOrder.getPayType() == QuanwaiOrder.PAY_ALI) {
            // 支付宝
            this.refundAliPay(quanwaiOrder, fee);
        }
    }

    private void refundAliPay(QuanwaiOrder quanwaiOrder, Double fee) {
        //商户订单号和支付宝交易号不能同时为空。 trade_no、  out_trade_no如果同时存在优先取trade_no
        //商户订单号，和支付宝交易号二选一
        String outTradeNo = quanwaiOrder.getOrderId();
        //退款金额，不能大于订单总金额
        String refundAmount = CommonUtils.formatePrice(fee);
        //退款的原因说明
        String refundReason = "";
        //标识一次退款请求，同一笔交易多次退款需要保证唯一，如需部分退款，则此参数必传。
        String outRequestNo = CommonUtils.randomString(16);
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        AlipayClient client = restfulHelper.initAlipayClient();
        AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();

        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(outTradeNo);
        //model.setTradeNo(trade_no);
        model.setRefundAmount(refundAmount);
        model.setRefundReason(refundReason);
        model.setOutRequestNo(outRequestNo);
        alipayRequest.setBizModel(model);

        AlipayTradeRefundResponse alipayResponse = null;
        try {
            alipayResponse = client.execute(alipayRequest);
            logger.info("订单：{},退款结果:{}", quanwaiOrder.getOrderId(), alipayResponse.getBody());
            if (alipayResponse.isSuccess()) {
                quanwaiOrderDao.refundOrder(quanwaiOrder.getOrderId(), fee, outRequestNo);
            } else {
                logger.error("response is------\n" + alipayResponse.getBody());
                messageService.sendAlarm("退款出错", "退款接口调用失败",
                        "高", "订单id:" + quanwaiOrder.getOrderId(), "msg:" + alipayResponse.getSubMsg() + ", error:" + alipayResponse.getMsg());
                throw new RefundException(alipayResponse.getMsg());
            }
        } catch (AlipayApiException e) {
            logger.error(e.getLocalizedMessage(), e);
            messageService.sendAlarm("退款出错", "退款接口调用失败",
                    "高", "订单id:" + quanwaiOrder.getOrderId(), "msg:" + e.getLocalizedMessage());
            throw new RefundException(e.getLocalizedMessage());
        }
    }

    private void refundWechatPay(QuanwaiOrder quanwaiOrder, Double fee) {
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

        String response = restfulHelper.sslPostXml(REFUND_ORDER_URL, XMLHelper.createXML(refundOrder));
        RefundOrderReply reply = XMLHelper.parseXml(RefundOrderReply.class, response);
        if (reply != null) {
            if (FAIL.equals(reply.getReturn_code()) || FAIL.equals(reply.getResult_code())) {
                logger.error("response is------\n" + response);
                messageService.sendAlarm("退款出错", "退款接口调用失败",
                        "高", "订单id:" + quanwaiOrder.getOrderId(), "msg:" + reply.getReturn_msg() + ", error:" + reply.getErr_code_des());
                throw new RefundException(reply.getReturn_msg());
            } else {
                quanwaiOrderDao.refundOrder(quanwaiOrder.getOrderId(), fee, refundOrder.getOut_refund_no());
            }

        }
    }

    @Override
    public String buildAlipayParam(QuanwaiOrder quanwaiOrder) {
        if (quanwaiOrder == null) {
            logger.error("order not existed");
            return "";
        }

        //获得初始化的AlipayClient
        AlipayClient alipayClient = restfulHelper.initAlipayClient();

        //创建API对应的request
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        //在公共参数中设置回跳和通知地址
        alipayRequest.setReturnUrl(ConfigUtils.getAlipayNotifyDomain() + ALIPAY_RETURN_PATH);
        alipayRequest.setNotifyUrl(ConfigUtils.getAlipayNotifyDomain() + ALIPAY_CALLBACK_PATH);
        //填充业务参数
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(quanwaiOrder.getOrderId());
        // 总价
        model.setTotalAmount(CommonUtils.formatePrice(quanwaiOrder.getPrice()));
        model.setSubject(quanwaiOrder.getGoodsName());
        // 固定的
        model.setProductCode("QUICK_WAP_PAY");
        // 手机网站支付2.0的交易超时时间只能设置相对时间吗
        model.setTimeoutExpress("1h");
        alipayRequest.setBizModel(model);
        String redirectParam = "";
        try {
            //调用SDK生成表单
            redirectParam = alipayClient.pageExecute(alipayRequest, "GET").getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        logger.info("redirect " + redirectParam);
        return redirectParam;
    }
}
