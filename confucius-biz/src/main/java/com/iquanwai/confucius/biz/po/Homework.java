package com.iquanwai.confucius.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/8/26.
 */
@Data
public class Homework {
    private int id;
    private String subject; //题干
    private String voice; //语音链接
    private Integer point; //分值
    private String pcurl; //pc端打开url
    private boolean submitted; //是否提交过
}
