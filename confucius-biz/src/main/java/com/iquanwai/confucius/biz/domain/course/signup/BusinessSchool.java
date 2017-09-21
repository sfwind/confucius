package com.iquanwai.confucius.biz.domain.course.signup;

import lombok.Data;

/**
 * Created by nethunder on 2017/9/20.
 */
@Data
public class BusinessSchool {
    private String startTime; // 开启时间 非DB字段
    private String endTime; // 结束时间 非DB字段
    private Double fee;
    /** 初始优惠金额 */
    private Double initDiscount;

    /** 已经是商学院会员 */
    private Boolean isBusinessStudent;

}
