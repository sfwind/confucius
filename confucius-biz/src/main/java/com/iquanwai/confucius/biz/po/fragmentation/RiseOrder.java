package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

/**
 * Created by nethunder on 2017/4/6.
 */
@Data
public class RiseOrder {
    private int id;
    private String orderId; //订单id
    private String openid; //openid
    private Integer memberType; // 会员类型
    private Boolean entry; //是否已报名 0-未报名,1-已报名
    private Boolean isDel; //是否已过期 0-未过期,1-已过期
    private String promoCode; //该订单使用的优惠码
}
