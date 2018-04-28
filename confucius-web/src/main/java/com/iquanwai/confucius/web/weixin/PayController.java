package com.iquanwai.confucius.web.weixin;

import com.iquanwai.confucius.biz.domain.course.signup.EntryManager;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.weixin.pay.OrderCallback;
import com.iquanwai.confucius.biz.domain.weixin.pay.OrderCallbackReply;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayCallback;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import com.iquanwai.confucius.biz.util.ThreadPool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by justin on 16/9/14.
 */
@RequestMapping("/wx/pay")
@Controller
public class PayController {
    @Autowired
    private PayService payService;
    @Autowired
    private EntryManager entryManager;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SUCCESS_RETURN = "<xml>\n" +
            "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
            "  <return_msg><![CDATA[OK]]></return_msg>\n" +
            "</xml>";

    @RequestMapping(value = "/order/callback", produces = "application/xml")
    public ResponseEntity<OrderCallbackReply> orderCallback(@RequestBody OrderCallback orderCallback) {
        logger.info(orderCallback.toString());
        OrderCallbackReply orderCallbackReply = null;
        try {
            //未关注用户先提示关注
            if ("N".equals(orderCallback.getIs_subscribe())) {
                logger.info("{}还没关注服务号", orderCallback.getOpenid());
                orderCallbackReply = payService.callbackReply(PayService.ERROR_CODE, "请先关注圈外服务号", "");
                return new ResponseEntity<>(orderCallbackReply, HttpStatus.OK);
            }
            String prepayId = payService.unifiedOrder(orderCallback.getProduct_id());
            if (StringUtils.isEmpty(prepayId)) {
                orderCallbackReply = payService.callbackReply(PayService.ERROR_CODE, "下单失败,请联系微信号quanwaizhushou", "");
            } else {
                orderCallbackReply = payService.callbackReply(PayService.SUCCESS_CODE, "下单成功", prepayId);
            }
            logger.info(orderCallbackReply.toString());
        } catch (Exception e) {
            //异常关闭订单
            logger.error("扫码支付回调处理失败", e);
        }
        return new ResponseEntity<>(orderCallbackReply, HttpStatus.OK);
    }

    @RequestMapping(value = "/result/risemember/callback")
    public void riseMemberPayCallback(@RequestBody PayCallback payCallback, HttpServletResponse response) throws IOException {
        logger.info("rise会员微信支付回调:{}", payCallback.toString());

        ThreadPool.execute(() -> {
            try {
                payService.handlePayResult(payCallback);
                if ("SUCCESS".equals(payCallback.getResult_code())) {
                    payService.payMemberSuccess(payCallback.getOut_trade_no());
                } else {
                    logger.error("{}付费失败", payCallback.getOut_trade_no());
                }
            } catch (Exception e) {
                logger.error("rise会员支付结果回调处理失败", e);
            }
        });

        response.setHeader("Content-Type", "application/xml");
        response.getWriter().print(SUCCESS_RETURN);
        response.flushBuffer();
    }

    @RequestMapping(value = "/result/risecamp/callback")
    public void riseTrainPayCallback(@RequestBody PayCallback payCallback, HttpServletResponse response) throws IOException {
        logger.info("专项课单卖微信支付回调：{}", payCallback.toString());

        ThreadPool.execute(() -> {
            try {
                payService.handlePayResult(payCallback);
                if ("SUCCESS".equals(payCallback.getResult_code())) {
                    entryManager.payMonthlyCampSuccess(payCallback.getOut_trade_no());
                } else {
                    logger.error("{}付费失败", payCallback.getOut_trade_no());
                }
            } catch (Exception e) {
                logger.error("专项课支付结果回调处理失败", e);
            }
        });

        response.setHeader("Content-Type", "application/xml");
        response.getWriter().print(SUCCESS_RETURN);
        response.flushBuffer();
    }

    @RequestMapping(value = "/result/application/callback")
    public void applicationPayCallback(@RequestBody PayCallback payCallback, HttpServletResponse response) throws IOException {
        logger.info("商学院申请微信支付回调：{}", payCallback.toString());

        ThreadPool.execute(() -> {
            try {
                payService.handlePayResult(payCallback);
                if ("SUCCESS".equals(payCallback.getResult_code())) {
                    entryManager.payApplicationSuccess(payCallback.getOut_trade_no());
                } else {
                    logger.error("{}付费失败", payCallback.getOut_trade_no());
                }
            } catch (Exception e) {
                logger.error("商学院申请支付结果回调处理失败", e);
            }
        });

        response.setHeader("Content-Type", "application/xml");
        response.getWriter().print(SUCCESS_RETURN);
        response.flushBuffer();
    }

}
