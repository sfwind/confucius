package com.iquanwai.confucius.biz.po;

import lombok.Data;

/**
 * Created by justin on 17/5/31.
 */
@Data
public class PromotionUser {
    private int id;
    private String openid; //被推荐人
    private String source; //来源
    private Integer action; // 用户行为（0-新人, 1-试用, 2-付费）
    private Integer profileId;  //推荐人id
}
