package com.iquanwai.confucius.biz.dao.po;

import lombok.Data;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class Material {
    private int id;
    private Integer type; //素材类型（1-文字，2-图片，3-语音，4-作业，5-题目，99-特殊）
    private Integer pageId; //篇id
    private Integer sequence; //篇内顺序
    private String content; //内容，文字或者链接
}
