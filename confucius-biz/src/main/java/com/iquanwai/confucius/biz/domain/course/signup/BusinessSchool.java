package com.iquanwai.confucius.biz.domain.course.signup;

import lombok.Data;

/**
 * Created by nethunder on 2017/9/20.
 */
@Data
public class BusinessSchool {
    private Double fee;
    /** 已经是商学院会员 */
    private Boolean isBusinessStudent;
}
