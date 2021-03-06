package com.iquanwai.confucius.web.payment.dto;

import lombok.Data;

/**
 * 专项课售卖页的信息
 * @author nethunder
 */
@Data
public class CampInfoDto {
    private String campPaymentImage;
    private String markSellingMemo; // 打点用到的，2017-10
    private Integer currentCampMonth;

}
