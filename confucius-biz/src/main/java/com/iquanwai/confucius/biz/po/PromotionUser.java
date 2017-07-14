package com.iquanwai.confucius.biz.po;

import lombok.Data;

/**
 * Created by justin on 17/5/31.
 */
@Data
public class PromotionUser {
    private int id;
    private String openid;
    private String source;
    private Boolean paid;
}
