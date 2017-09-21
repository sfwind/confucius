package com.iquanwai.confucius.web.course.dto.payment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.common.customer.CourseReductionActivity;
import lombok.Data;

import java.util.List;
import java.util.Map;

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
    /** 商品的Id，如果是会员，则对应memberTypeId，如果是小课，则对应ProblemID */
    private Integer goodsId;
    /** 获取优惠券 */
    private List<Coupon> coupons;
    /** 开启时间 非DB字段 */
    private String startTime;
    /** 结束时间 非DB字段 */
    private String endTime;
    /** 活动状态 */
    private CourseReductionActivity activity;
    private Double initDiscount; // 初始优惠
    /** 是否可以使用多个优惠券 */
    private Boolean multiCoupons;


    /** 商品类型，这个主要用来判断商品类型是否正确 */
    public static final List<String> GOODS_TYPES = Lists.newArrayList();
    /** 商品name */
    public static final Map<String, String> GOODS_NAMES = Maps.newHashMap();


    static {
        // 初始化商品列表，下面的常量也需要定义
        GOODS_TYPES.add(QuanwaiOrder.FRAG_CAMP);
        GOODS_TYPES.add(QuanwaiOrder.FRAG_MEMBER);

//        GOODS_NAMES.put(FRAG_COURSE, "小课购买");
        GOODS_NAMES.put(QuanwaiOrder.FRAG_MEMBER, "圈外商学院");
        GOODS_NAMES.put(QuanwaiOrder.FRAG_CAMP, "小课训练营");
        // 299 原价
        // 专业一年  原价-880
        // 专业半年 原价-580
        // 其他 需要申请 原价
    }

}
