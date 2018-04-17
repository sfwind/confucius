package com.iquanwai.confucius.web.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayCallback;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import com.iquanwai.confucius.biz.util.ThreadPool;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * @author nethunder
 */
@Controller
@RequestMapping("/ali/pay")
public class AliPayController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * <p>交易完成，触发方式：</p>
     * <ul>
     * <li>可退款交易，超过退款期限，则支付宝会推送该消息</li>
     * <li>不可退款交易，在支付完成后会立刻推送该消息</li>
     * </ul>
     */
    public static final String ALIPAY_TRADE_FINISHED = "TRADE_FINISHED";
    /**
     * <p>交易成功，触发方式：</p>
     * <ul>
     * <li>可退款交易，在支付完成后会立刻推送该消息</li>
     * </ul>
     */
    public static final String ALIPAY_TRADE_SUCCESS = "TRADE_SUCCESS";


    @Autowired
    private PayService payService;
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private SignupService signupService;
    @Autowired
    private MessageService messageService;


    @RequestMapping(value = "order/query", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> queryOrder(@RequestParam String order) throws AlipayApiException {
        //商户订单号，商户网站订单系统中唯一订单号，必填
        /**********************/
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        AlipayClient client = restfulHelper.initAlipayClient();
        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();

        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(order);
        alipayRequest.setBizModel(model);

        AlipayTradeQueryResponse alipayResponse = client.execute(alipayRequest);
        logger.info(alipayResponse.getBody());
        return WebUtils.result(alipayResponse.getBody());
    }

    @RequestMapping(value = "callback/notify", method = RequestMethod.POST)
    public void alipayNotifyCallback(HttpServletRequest request, HttpServletResponse response) {
        try {
            PrintWriter out = response.getWriter();
            Map<String, String> params = Maps.newHashMap();
            Map requestParams = request.getParameterMap();
            for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }
            //商户订单号
            String outTradeNo = request.getParameter("out_trade_no");
            //支付宝交易号
            String tradeNo = request.getParameter("trade_no");
            //交易状态
            String tradeStatus = request.getParameter("trade_status");
            //交易结束时间
            String paymentTime = request.getParameter("gmt_payment");

            PayCallback payCallback = new PayCallback();
            payCallback.setTime_end(paymentTime);
            payCallback.setTransaction_id(tradeNo);
            payCallback.setOut_trade_no(outTradeNo);
            payCallback.setResult_code(tradeStatus);
            boolean verifyResult = AlipaySignature.rsaCheckV1(params, ConfigUtils.getValue("alipay.public.key"),
                    "UTF-8", AlipayConstants.SIGN_TYPE_RSA2);
            logger.info("进入回调.\n 订单号：{} \n 交易号：{} \n 交易状态：{} \n 验签结果：{}", outTradeNo, tradeNo, tradeStatus, verifyResult);
            if (verifyResult) {
                // 验证成功
                tradeBusinessDeal(payCallback);
                try {
                    out.println("success");
                    response.flushBuffer();
                } catch (IOException e1) {
                    logger.error(e1.getLocalizedMessage(), e1);
                } finally {
                    IOUtils.closeQuietly(out);
                }
            } else {
                // 验证失败
                try {
                    out.println("fail");
                    response.flushBuffer();
                } catch (IOException e1) {
                    logger.error(e1.getLocalizedMessage(), e1);
                } finally {
                    IOUtils.closeQuietly(out);
                }
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 订单的业务处理
     */
    private void tradeBusinessDeal(PayCallback payCallback) {
        logger.info("处理订单业务逻辑,商户订单号:{},支付宝交易号:{},交易状态:{}", payCallback.getOut_trade_no(),
                payCallback.getTransaction_id(),
                payCallback.getResult_code());
        ThreadPool.execute(() -> {
            try {
                QuanwaiOrder quanwaiOrder = signupService.getQuanwaiOrder(payCallback.getOut_trade_no());
                if (quanwaiOrder.getStatus() != QuanwaiOrder.UNDER_PAY) {
                    return;
                }
                // if (ALIPAY_TRADE_FINISHED.equals(payCallback.getResult_code())) {
                //     //注意：
                //     //如果签约的是可退款协议，退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
                //     //如果没有签约可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
                //
                // } else if (ALIPAY_TRADE_SUCCESS.equals(payCallback.getResult_code())) {
                //     //注意：
                //     //如果签约的是可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
                // }
                if (ALIPAY_TRADE_FINISHED.equals(payCallback.getResult_code()) || ALIPAY_TRADE_SUCCESS.equals(payCallback.getResult_code())) {
                    payService.handlePayResult(payCallback);
                    if (QuanwaiOrder.FRAG_MEMBER.equals(quanwaiOrder.getGoodsType())) {
                        payService.payMemberSuccess(quanwaiOrder.getOrderId());
                    } else if (QuanwaiOrder.FRAG_CAMP.equals(quanwaiOrder.getGoodsType())) {
                        signupService.payMonthlyCampSuccess(quanwaiOrder.getOrderId());
                    } else if (QuanwaiOrder.BS_APPLICATION.equals(quanwaiOrder.getGoodsType())) {
                        signupService.payApplicationSuccess(quanwaiOrder.getOrderId());
                    }
                }
            } catch (Exception e) {
                messageService.sendAlarm("报名模块出错", "阿里回调处理失败",
                        "高", "orderId:" + payCallback.getOut_trade_no(), e.getLocalizedMessage());
                logger.error("rise会员支付结果回调处理失败", e);

            }
        });
    }

}
