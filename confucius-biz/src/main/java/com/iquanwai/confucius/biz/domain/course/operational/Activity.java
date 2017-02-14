package com.iquanwai.confucius.biz.domain.course.operational;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 17/2/14.
 */
@Data
public class Activity {
    private String name; //活动名称
    private Integer promoCodeUsageLimit; //优惠码使用上限
    private Date startDate; //开始时间
    private Date endDate; //结束时间
    private Double discount; //优惠金额

    public Activity name(String name){
        this.name = name;
        return this;
    }

    public Activity promoCodeUsageLimit(Integer promoCodeUsageLimit){
        this.promoCodeUsageLimit = promoCodeUsageLimit;
        return this;
    }

    public Activity discount(Double discount){
        this.discount = discount;
        return this;
    }

    public Activity endDate(Date endDate){
        this.endDate = endDate;
        return this;
    }
}
