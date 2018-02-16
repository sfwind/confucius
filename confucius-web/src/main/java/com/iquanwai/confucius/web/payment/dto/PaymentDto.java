package com.iquanwai.confucius.web.payment.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2017/8/17.
 */
@Data
public class PaymentDto {
//    private Integer couponId; // 优惠券id
    private List<Integer> couponsIdGroup; // 一组优惠券
    private Double fee; //实际金额
    private boolean isFree = false; //是否免费
    private String productId; //订单id,即orderId
    private Map<String, String> signParams;
    /**
     * 支付类型，默认是微信h5支付<br/>
     * 1-微信h5支付<br/>
     * 2-支付宝手机支付<br/>
     */
    private Integer payType=1;

    private Integer goodsId;
    private String goodsType;
}
