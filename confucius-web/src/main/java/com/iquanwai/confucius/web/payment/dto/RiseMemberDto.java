package com.iquanwai.confucius.web.payment.dto;

import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/4/6.
 */
@Data
public class RiseMemberDto {
    private String openId;
    private List<Integer> couponIdGroup;

    private List<MemberType> memberTypes;
    private MemberType memberType;
    private String tip;
    /** 是否有报名权限 */
    private Boolean privilege;
    private String errorMsg;
    /** 已经是商学院会员 */
    private Boolean elite;
    /**
     * 报名按钮显示
     */
    private String buttonStr;
    /**
     * 试听课按钮显示
     */
    private String auditionStr;
    /** 订单关闭剩余小时 */
    private int remainHour;
    /** 订单关闭剩余分钟 */
    private int remainMinute;
}
