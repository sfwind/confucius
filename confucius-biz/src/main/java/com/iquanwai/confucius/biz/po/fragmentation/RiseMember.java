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
    private String memo;
    private Integer del;
    private Date addTime; //添加时间

    private String startTime; // 非DB字段，addTime
    private String endTime; // 非DB字段，expireDate
    private String name; //非DB字段
    private String entryCode; //入群密令

    public RiseMember simple() {
        RiseMember riseMember = new RiseMember();
        riseMember.setMemberTypeId(memberTypeId);
        riseMember.setExpireDate(expireDate);
        riseMember.setExpired(expired);
        riseMember.setAddTime(addTime);
        riseMember.setStartTime(startTime);
        riseMember.setEndTime(endTime);
        riseMember.setName(name);
        riseMember.setEntryCode(entryCode);
        return riseMember;
    }

    public static final int HALF = 1;
    public static final int ANNUAL = 2;
    public static final int ELITE = 3;//精英用户
    public static final int HALF_ELITE = 4; // 精英版半年
    public static final int CAMP = 5; // 小课训练营
    public static final int COURSE = 6; // 单买小课
}
