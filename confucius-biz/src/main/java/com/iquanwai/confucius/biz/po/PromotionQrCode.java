package com.iquanwai.confucius.biz.po;

import lombok.Data;

@Data
public class PromotionQrCode {
    private Integer id;
    /**
     * 场景值
     */
    private String scene;
    private String url;
    /**
     * 应用场景
     */
    private String remark;
}
