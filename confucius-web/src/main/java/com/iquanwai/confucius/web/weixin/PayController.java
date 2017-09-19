package com.iquanwai.confucius.web.weixin;

import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.weixin.pay.OrderCallback;
import com.iquanwai.confucius.biz.domain.weixin.pay.OrderCallbackReply;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayCallback;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by justin on 16/9/14.
 */
@RequestMapping("/wx/pay")
@Controller
public class PayController {
    @Autowired
    private PayService payService;
    @Autowired
    private SignupService signupService;

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        executorService = new ThreadPoolExecutor(5, 10, 1, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(50, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final String SUCCESS_RETURN = "<xml>\n" +
            "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
            "  <return_msg><![CDATA[OK]]></return_msg>\n" +
            "</xml>";

    @RequestMapping(value = "/order/callback", produces = "application/xml")
    public ResponseEntity<OrderCallbackReply> orderCallback(@RequestBody OrderCallback orderCallback) {
        LOGGER.info(orderCallback.toString());
        OrderCallbackReply orderCallbackReply = null;
        try {
            //未关注用户先提示关注
            if ("N".equals(orderCallback.getIs_subscribe())) {
                LOGGER.info("{}还没关注服务号", orderCallback.getOpenid());
                orderCallbackReply = payService.callbackReply(PayService.ERROR_CODE, "请先关注圈外服务号", "");
                return new ResponseEntity<>(orderCallbackReply, HttpStatus.OK);
            }
            String prepayId = payService.unifiedOrder(orderCallback.getProduct_id());
            if (StringUtils.isEmpty(prepayId)) {
                orderCallbackReply = payService.callbackReply(PayService.ERROR_CODE, "下单失败,请联系微信号quanwaizhushou", "");
            } else {
                orderCallbackReply = payService.callbackReply(PayService.SUCCESS_CODE, "下单成功", prepayId);
            }
            LOGGER.info(orderCallbackReply.toString());
        } catch (Exception e) {
            //异常关闭订单
            payService.closeOrder(orderCallback.getProduct_id());
            LOGGER.error("扫码支付回调处理失败", e);
        }

        return new ResponseEntity<>(orderCallbackReply, HttpStatus.OK);
    }

    @RequestMapping(value = "/result/callback")
    public void payCallback(@RequestBody PayCallback payCallback, HttpServletResponse response) throws IOException {
        LOGGER.info("体系化微信支付回调:{}", payCallback.toString());
        try {
            payService.handlePayResult(payCallback);
            if (payCallback.getResult_code().equals("SUCCESS")) {
                payService.paySuccess(payCallback.getOut_trade_no());
            } else {
                LOGGER.error("{}付费失败", payCallback.getOut_trade_no());
            }
        } catch (Exception e) {
            LOGGER.error("支付结果回调处理失败", e);
        }

        response.setHeader("Content-Type", "application/xml");
        response.getWriter().print(SUCCESS_RETURN);
        response.flushBuffer();
    }

    @RequestMapping(value = "/result/risemember/callback")
    public void riseMemberPayCallback(@RequestBody PayCallback payCallback, HttpServletResponse response) throws IOException {
        LOGGER.info("rise会员微信支付回调:{}", payCallback.toString());
        try {
            payService.handlePayResult(payCallback);
            if (payCallback.getResult_code().equals("SUCCESS")) {
                payService.payMemberSuccess(payCallback.getOut_trade_no());
            } else {
                LOGGER.error("{}付费失败", payCallback.getOut_trade_no());
            }
        } catch (Exception e) {
            LOGGER.error("rise会员支付结果回调处理失败", e);
        }
        response.setHeader("Content-Type", "application/xml");
        response.getWriter().print(SUCCESS_RETURN);
        response.flushBuffer();
    }

    @RequestMapping(value = "/result/risecourse/callback")
    public void riseCoursePayCallback(@RequestBody PayCallback payCallback, HttpServletResponse response) throws IOException {
        LOGGER.info("rise小课单卖微信支付回调:{}", payCallback.toString());
        try {
            payService.handlePayResult(payCallback);
            if (payCallback.getResult_code().equals("SUCCESS")) {
                payService.payFragmentSuccess(payCallback.getOut_trade_no());
            } else {
                LOGGER.error("{}付费失败", payCallback.getOut_trade_no());
            }
        } catch (Exception e) {
            LOGGER.error("小课单卖支付结果回调处理失败", e);
        }
        response.setHeader("Content-Type", "application/xml");
        response.getWriter().print(SUCCESS_RETURN);
        response.flushBuffer();
    }

    @RequestMapping(value = "/result/risecamp/callback")
    public void riseTrainPayCallback(@RequestBody PayCallback payCallback, HttpServletResponse response) throws IOException {
        LOGGER.info("训练营小课单卖微信支付回调：{}", payCallback.toString());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    payService.handlePayResult(payCallback);
                    if (payCallback.getResult_code().equals("SUCCESS")) {
                        signupService.payMonthlyCampSuccess(payCallback.getOut_trade_no());
                    } else {
                        LOGGER.error("{}付费失败", payCallback.getOut_trade_no());
                    }
                } catch (Exception e) {
                    LOGGER.error("小课单卖支付结果回调处理失败", e);
                }
            }
        });

        response.setHeader("Content-Type", "application/xml");
        response.getWriter().print(SUCCESS_RETURN);
        response.flushBuffer();
    }

}
