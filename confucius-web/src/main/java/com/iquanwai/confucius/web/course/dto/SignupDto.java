package com.iquanwai.confucius.web.course.dto;

import com.iquanwai.confucius.biz.po.PromoCode;
import com.iquanwai.confucius.biz.po.systematism.CourseIntroduction;
import com.iquanwai.confucius.biz.po.systematism.QuanwaiClass;
import lombok.Data;

/**
 * Created by justin on 16/9/10.
 */
@Data
public class SignupDto {
    private Integer remaining;
    private String qrcode;
    private CourseIntroduction course;
    private String productId;
    private Double fee; //实际金额
    private Double normal; //正常金额
    private Double discount; //折扣金额
    private boolean isFree = false;
    private QuanwaiClass quanwaiClass;
    private PromoCode promoCode;
}
