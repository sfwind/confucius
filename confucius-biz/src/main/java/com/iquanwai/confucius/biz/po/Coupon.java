package com.iquanwai.confucius.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 16/9/14.
 */
@Data
public class Coupon {
    private int id;
    private String openid;
    private Integer amount;
    private Integer used; //是否使用（0-否，1-是，2-正在使用）
    private Date expiredDate; //过期日期
}
