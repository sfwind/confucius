package com.iquanwai.confucius.web.payment;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.resolver.LoginUser;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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

    @Autowired
    private PayService payService;

    @RequestMapping(value = "/form")
    public void getPayForm(HttpServletRequest httpRequest,
                           HttpServletResponse httpResponse, LoginUser loginUser) throws Exception {
        AlipayClient alipayClient = new DefaultAlipayClient(ConfigUtils.getValue("alipay.gateway"),
                ConfigUtils.getValue("alipay.appid"),
                ConfigUtils.getValue("alipay.private.key"),
                "json",
                "UTF-8",
                ConfigUtils.getValue("alipay.public.key"),
                "RSA2"); //获得初始化的AlipayClient


        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
        alipayRequest.setReturnUrl("http://zzk.confucius.mobi/ali/pay/callback/return");
        alipayRequest.setNotifyUrl("http://zzk.confucius.mobi/ali/pay/callback/notify");//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\"" + CommonUtils.randomString(32) + "\"," +
                " \"total_amount\":\"0.01\"," +
                " \"subject\":\"圈外商学院\"," +
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }");//填充业务参数
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + "UTF-8");
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

    @RequestMapping(value = "callback/notify", method = RequestMethod.POST)
    public void alipayNotifyCallback(HttpServletRequest request, HttpServletResponse response) {
        //获取支付宝POST过来反馈信息
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
                //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
                //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
                params.put(name, valueStr);
            }
            logger.info("notify requestParams:{}", requestParams);
            logger.info("notify param:{}", params);
            //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
            //商户订单号

            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
            //支付宝交易号

            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");

            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");

            //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
            //计算得出通知验证结果
            //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
            boolean verify_result = AlipaySignature.rsaCheckV2(params, ConfigUtils.getValue("alipay.public.key"),
                    "UTF-8", "RSA2");
            logger.info("进入回调,{},{},{},{}", out_trade_no, trade_no, trade_status, verify_result);

            if (verify_result) {//验证成功
                //////////////////////////////////////////////////////////////////////////////////////////
                //请在这里加上商户的业务逻辑程序代码
                //——请根据您的业务逻辑来编写程序（以下代码仅作参考）——

                if ("TRADE_FINISHED".equals(trade_status)) {
                    //判断该笔订单是否在商户网站中已经做过处理
                    //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                    //如果有做过处理，不执行商户的业务程序

                    //注意：
                    //如果签约的是可退款协议，退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
                    //如果没有签约可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
                } else if ("TRADE_SUCCESS".equals(trade_status)) {
                    //判断该笔订单是否在商户网站中已经做过处理
                    //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                    //如果有做过处理，不执行商户的业务程序

                    //注意：
                    //如果签约的是可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
                }

                //——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
                try {
                    response.setHeader("Content-Type", "text/plain");
                    logger.info("return success");
                    out.println("success");    //请不要修改或删除
                    response.flushBuffer();
                } catch (IOException e1) {
                    logger.error(e1.getLocalizedMessage(), e1);
                } finally {
                    IOUtils.closeQuietly(out);
                }
                //////////////////////////////////////////////////////////////////////////////////////////
            } else {//验证失败
                try {
                    response.setHeader("Content-Type", "text/plain");
                    logger.info("return fail");
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


}
