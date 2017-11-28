package com.iquanwai.confucius.biz.po.common.customer;

import lombok.Data;

/**
 * Created by nethunder on 2017/9/6.
 */
@Data
public class CustomerStatus {
    private Integer profileId;
    private Integer statusId;
    private Boolean del;

    public static final Integer OPEN_BIBLE = 1; //开bible
    public static final Integer EDIT_TAG = 2; //选择tag
    public static final Integer APPLY_BUSINESS_SCHOOL_SUCCESS = 3; // 申请通过
    /**
     * 学过试听课
     * @deprecated
     */
    public static final Integer LEARNED_AUDITION = 4;
    /**
     * 是否不用开课程表
     */
    public static final Integer SCHEDULE_LESS = 5;
}
