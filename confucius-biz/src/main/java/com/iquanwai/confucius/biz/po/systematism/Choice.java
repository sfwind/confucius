package com.iquanwai.confucius.biz.po.systematism;

import lombok.Data;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class Choice {
    private int id;
    private Integer questionId; //问题id
    private String subject; //题干
    private Integer sequence; //选项序号
    private Boolean right; //是否正确（1-是，0-否）
}
