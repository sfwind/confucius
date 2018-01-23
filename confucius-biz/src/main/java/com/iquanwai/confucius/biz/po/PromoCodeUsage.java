package com.iquanwai.confucius.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 17/2/13.
 */
@Data
public class PromoCodeUsage {
    private int	id;
    private Integer promoCodeId; //优惠码id
    private String user;  //使用者
    private Date addTime; //使用时间
}
