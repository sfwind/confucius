package com.iquanwai.confucius.web.payment;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
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

    /**
     * 测试接口，待删除
     */
    @RequestMapping(value = "/form")
    public void getPayForm(HttpServletRequest httpRequest,
                           HttpServletResponse httpResponse) throws Exception {

        AlipayClient alipayClient = new DefaultAlipayClient(ConfigUtils.getValue("alipay.gateway"),
                ConfigUtils.getValue("alipay.appid"),
                ConfigUtils.getValue("alipay.private.key"),
                "json",
                "UTF-8",
                ConfigUtils.getValue("alipay.public.key"),
                "RSA2");

        //获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        //创建API对应的request
        alipayRequest.setReturnUrl("http://zzk.confucius.mobi/ali/pay/callback/return");
        //在公共参数中设置回跳和通知地址
        alipayRequest.setNotifyUrl("http://zzk.confucius.mobi/ali/pay/callback/notify");
        //填充业务参数
        Map<String,String> bizContent = Maps.newHashMap();
        bizContent.put("out_trade_no", CommonUtils.randomString(32));
        bizContent.put("total_amount", "0.01");
        bizContent.put("subject", "圈外商学院");
        bizContent.put("product_code", "QUICK_WAP_PAY");
        bizContent.put("timeout_express", "1m");
        alipayRequest.setBizContent(JSON.toJSONString(bizContent));
        String form = "";
        try {
            //调用SDK生成表单
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        httpResponse.setContentType("text/html;charset=" + "UTF-8");
        //直接将完整的表单html输出到页面
        httpResponse.getWriter().write(form);
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

    @RequestMapping(value = "callback/notify", method = RequestMethod.POST)
    public void alipayNotifyCallback(HttpServletRequest request, HttpServletResponse response) {
        try {
            //获取支付宝POST过来反馈信息
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
            //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
            //商户订单号
            String outTradeNo = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
            //支付宝交易号
            String tradeNo = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
            //交易状态
            String tradeStatus = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");

            //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
            //计算得出通知验证结果
            //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
            boolean verifyResult = AlipaySignature.rsaCheckV1(params, ConfigUtils.getValue("alipay.public.key"),
                    "UTF-8", AlipayConstants.SIGN_TYPE_RSA2);
            logger.info("进入回调.\n 订单号：{} \n 交易号：{} \n 交易状态：{} \n 验签结果：{}", outTradeNo, tradeNo, tradeStatus, verifyResult);
            if (verifyResult) {
                //验证成功
                //////////////////////////////////////////////////////////////////////////////////////////
                //请在这里加上商户的业务逻辑程序代码
                //——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
                if (ALIPAY_TRADE_FINISHED.equals(tradeStatus)) {
                    //判断该笔订单是否在商户网站中已经做过处理
                    //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                    //如果有做过处理，不执行商户的业务程序
                    tradeBusinessDeal(outTradeNo, tradeNo, tradeStatus);
                    //注意：
                    //如果签约的是可退款协议，退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
                    //如果没有签约可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
                } else if (ALIPAY_TRADE_SUCCESS.equals(tradeStatus)) {
                    //判断该笔订单是否在商户网站中已经做过处理
                    //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                    //如果有做过处理，不执行商户的业务程序
                    tradeBusinessDeal(outTradeNo, tradeNo, tradeStatus);
                    //注意：
                    //如果签约的是可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
                }

                //——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
                try {
                    response.setHeader("Content-Type", "text/plain");
                    //请不要修改或删除
                    out.println("success");
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
     *
     * @param outTradeNo 商户订单号
     * @param tradeNo 支付宝交易号
     * @param tradeStatus 交易状态
     */
    private void tradeBusinessDeal(String outTradeNo, String tradeNo, String tradeStatus) {
        logger.info("处理订单业务逻辑,商户订单号:{},支付宝交易号:{},交易状态:{}", outTradeNo, tradeNo, tradeStatus);

    }


}
