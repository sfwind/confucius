package com.iquanwai.confucius.biz.po.systematism;

import lombok.Data;

/**
 * Created by justin on 16/9/10.
 */
@Data
public class CourseOrder {
    private int id;
    private String orderId; //订单id
    private String openid; //openid
    private Integer courseId; //课程id
    private Integer classId; //班级id
    private Boolean entry; //是否已报名 0-未报名,1-已报名
    private Boolean isDel; //是否已过期 0-未过期,1-已过期
    private String promoCode; //该订单使用的优惠码

}
