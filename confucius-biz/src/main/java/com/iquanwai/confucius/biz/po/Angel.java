package com.iquanwai.confucius.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/10/11.
 */
@Data
public class Angel {
    private int id;
    private String memberId; //学号
    private String angelId; //守护学员的学号
    private Integer classId; //班级id
}
