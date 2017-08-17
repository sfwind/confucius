package com.iquanwai.confucius.web.course.dto.payment;

import lombok.Data;

import java.util.Map;

/**
 * Created by nethunder on 2017/8/17.
 */
@Data
public class PaymentDto {
    private Integer couponId; // 优惠券id
    private Double fee; //实际金额
    private boolean isFree = false; //是否免费
    private String productId; //订单id,即orderId
    private Map<String,String> signParams;

    private Integer goodsId;
    private String goodsType;
}
