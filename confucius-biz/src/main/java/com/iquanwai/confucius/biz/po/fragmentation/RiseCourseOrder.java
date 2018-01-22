package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

/**
 * Created by nethunder on 2017/7/14.
 */
@Data
public class RiseCourseOrder {
    private Integer id;
    private Integer profileId;
    private Integer problemId;
    private String orderId;
    private Boolean entry;
    private Boolean isDel;
}
