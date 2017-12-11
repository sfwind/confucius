package com.iquanwai.confucius.biz.po.common.customer;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/9/27.
 */
@Data
public class BusinessSchoolApplication {
    private Integer id;
    private Integer submitId;
    private Integer profileId;
    private String openid;
    private Integer status;
    private Date checkTime;
    private Double coupon;
    private Boolean deal;
    private Integer originMemberType;
    private Boolean del;
    private Boolean isDuplicate;
    private String comment;
    private Date dealTime;
    private Date addTime;
    private String orderId;

    private String originMemberTypeName;

    public static final int APPLYING = 0;
    public static final int APPROVE = 1;
    public static final int REJECT = 2;
    public static final int IGNORE = 3;

}
