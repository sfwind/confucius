package com.iquanwai.confucius.web.course.dto;

import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/4/6.
 */
@Data
public class RiseMemberDto {
    private Integer memberType;
    private String openId;
//    private Integer couponId;
    private List<Integer> couponIdGroup;

    private List<MemberType> memberTypes;
    private List<Coupon> coupons;
}
