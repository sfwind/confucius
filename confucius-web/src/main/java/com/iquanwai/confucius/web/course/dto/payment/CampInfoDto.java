package com.iquanwai.confucius.web.course.dto.payment;

import lombok.Data;

/**
 * 训练营售卖页的信息
 * @author nethunder
 */
@Data
public class CampInfoDto {
    private String campPaymentImage;
    private String markSellingMemo; // 打点用到的，2017-10
    private Integer currentCampMonth;

}
