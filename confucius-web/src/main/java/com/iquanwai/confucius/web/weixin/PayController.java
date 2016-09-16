package com.iquanwai.confucius.web.weixin;

import com.iquanwai.confucius.biz.domain.weixin.pay.OrderCallback;
import com.iquanwai.confucius.biz.domain.weixin.pay.OrderCallbackReply;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayCallback;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import com.iquanwai.confucius.biz.util.XMLHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final String SUCCESS_RETURN = "<xml>\n" +
            "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
            "  <return_msg><![CDATA[OK]]></return_msg>\n" +
            "</xml>";

    @RequestMapping(value="/order/callback", produces = "application/xml")
    public ResponseEntity<OrderCallbackReply> orderCallback(@RequestBody OrderCallback orderCallback) {
        LOGGER.info(orderCallback.toString());
        OrderCallbackReply orderCallbackReply;
        //未关注用户先提示关注
        if(orderCallback.equals("N")){
            LOGGER.info("{}还没关注服务号", orderCallback.getOpenid());
            orderCallbackReply = payService.callbackReply(PayService.ERROR_CODE, "请先关注圈外服务号", "");
            return new ResponseEntity<OrderCallbackReply>(orderCallbackReply, HttpStatus.OK);
        }
        String prepayId = payService.unifiedOrder(orderCallback.getProduct_id());
        if(StringUtils.isEmpty(prepayId)){
            orderCallbackReply = payService.callbackReply(PayService.ERROR_CODE, "支付失败，请重新扫描二维码", "");
        }else{
            orderCallbackReply = payService.callbackReply(PayService.SUCCESS_CODE, "支付成功", prepayId);
        }
        return new ResponseEntity<OrderCallbackReply>(orderCallbackReply, HttpStatus.OK);
    }

    @RequestMapping(value="/pay/callback")
    public void payCallback(@RequestBody PayCallback payCallback, HttpServletResponse response) throws IOException {
        LOGGER.info(payCallback.toString());
        try {
            payService.handlePayResult(payCallback);
        }catch (Exception e){
            LOGGER.error("支付结果回调处理失败", e);
        }
        response.setHeader("Content-Type", "application/xml");
        response.getWriter().print(SUCCESS_RETURN);
        response.flushBuffer();
    }
}
