package com.iquanwai.confucius.biz.dao.po;

import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * Created by justin on 16/8/26.
 */
@Data
@Alias("homework")
public class Homework {
    private int id;
    private Integer materialId; //素材id
    private String subject; //题干
    private String voice; //语音链接
    private Integer point; //分值
    private String pcurl; //pc端打开url
}
