package com.iquanwai.confucius.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 17/2/13.
 */
@Data
public class PromoCode {
    private int	id; //自增id
    private String promoCode; //优惠码
    private String activityCode; //活动id
    private Integer useCount;	//使用次数
    private String openid;  //拥有者openid
    private Date expiredDate; //过期时间
    private Double dscount; //折扣金额
}
