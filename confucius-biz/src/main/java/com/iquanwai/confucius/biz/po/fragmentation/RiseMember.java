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
    private Integer profileId; //用户id
    private Integer memberTypeId; //会员类型1-专业版半年,2-专业版一年,3-精英版,4-精英版半年,5-专项课
    private Date expireDate; //过期时间
    private Boolean expired; //是否过期（0-否,1-是）
    private String memo;
    private Boolean vip; // 是否是 vip
    private Integer del;
    private Date addTime; //添加时间
    /**
     * 开营时间
     */
    private Date openDate;

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
    public static final int CAMP = 5; // 专项课
    public static final int COURSE = 6; // 单买课程
    public static final int BS_APPLICATION = 7; // 商学院申请
    public static final int BUSINESS_THOUGHT = 8; // 商业思维
    /**
     * 商业思维申请
     */
    public static final int BUSINESS_THOUGHT_APPLY = 9;

    public static boolean isApply(Integer memberTypeId) {
        return memberTypeId == BS_APPLICATION || memberTypeId == BUSINESS_THOUGHT_APPLY;
    }

    public static boolean isMember(Integer memberTypeId) {
        return memberTypeId == ELITE || memberTypeId == BUSINESS_THOUGHT;
    }

    public static boolean isProfileSet(Integer memberTypeId) {
        return memberTypeId == ELITE || memberTypeId == BUSINESS_THOUGHT || memberTypeId == CAMP;
    }


    public static boolean isProfileSet(String memberTypeId) {
        return isProfileSet(Integer.valueOf(memberTypeId));
    }

}
