package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class WarmupChoice {
    private int id;
    private Integer questionId; //问题id
    private String subject; //题干
    private Integer sequence; //选项序号
    private Boolean isRight; //是否正确（1-是，0-否）
    private Boolean selected; //非db字段 用户是否选择
    private Integer updated;//更新字段
}
