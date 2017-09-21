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
    private Integer goodsId;

}
