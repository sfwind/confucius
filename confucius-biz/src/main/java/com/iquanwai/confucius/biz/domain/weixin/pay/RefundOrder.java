package com.iquanwai.confucius.biz.domain.weixin.pay;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by justin on 2017/10/25.
 */
@XmlRootElement(name="xml")
@Data
public class RefundOrder {
    private String appid; //公众账号ID
    private String mch_id; //商户号
    private String nonce_str; //随机字符串
    private String sign; //签名
    private int total_fee; //订单金额
    private int refund_fee; //退款金额
    private String out_refund_no; //退款单号
    private String out_trade_no; //订单号
}
