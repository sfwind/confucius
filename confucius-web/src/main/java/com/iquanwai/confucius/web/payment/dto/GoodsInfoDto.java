package com.iquanwai.confucius.web.payment.dto;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.common.customer.CourseReductionActivity;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/8/17.
 */
@Data
public class GoodsInfoDto {
    /** 商品类型 */
    private String goodsType;
    /** 费用 */
    private Double fee;
    /** 商品名 */
    private String name;
    /** 商品的Id，如果是会员，则对应memberTypeId，如果是课程，则对应ProblemID */
    private Integer goodsId;
    /** 获取优惠券 */
    private List<Coupon> coupons;
    /** 开启时间 非DB字段 */
    private String startTime;
    /** 结束时间 非DB字段 */
    private String endTime;
    /** 活动状态 */
    private CourseReductionActivity activity;
    private Double initPrice;// 原价
    /** 是否可以使用多个优惠券 */
    private Boolean multiCoupons;
    /** 自动选择优惠券 */
    private List<Coupon> autoCoupons;


    /** 商品类型，这个主要用来判断商品类型是否正确 */
    public static final List<String> GOODS_TYPES = Lists.newArrayList();


    static {
        // 初始化商品列表，下面的常量也需要定义
        GOODS_TYPES.add(QuanwaiOrder.FRAG_CAMP);
        GOODS_TYPES.add(QuanwaiOrder.FRAG_MEMBER);
        GOODS_TYPES.add(QuanwaiOrder.BS_APPLICATION);
    }

}
