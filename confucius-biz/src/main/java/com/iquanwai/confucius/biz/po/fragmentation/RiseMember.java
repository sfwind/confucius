package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/4/13.
 */
@Data
public class RiseMember {
    private Integer id;
    private String orderId; //订单id
    private String openId; //openid
    private Integer profileId; //用户id
    private Integer memberTypeId; //会员类型1-专业版半年,2-专业版一年,3-精英版,4-小课训练营
    private Date expireDate; //过期时间
    private Boolean expired; //是否过期（0-否,1-是）
    private Date addTime; //添加时间

    private String startTime; // 非DB字段，addTime
    private String endTime; // 非DB字段，expireDate
    private String name; //非DB字段

    public static final int ELITE =3;//精英用户
    public static final int HALF_ELITE = 4;
}
