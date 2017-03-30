package com.iquanwai.confucius.biz.po.performance;

import lombok.Data;

import java.util.Date;

/**
 * Created by shen on 17/3/8.
 */
@Data
public class PersonalPerformance {
    private int id;
    private String key;
    private String name;
    private int time;
    private Date addTime;
}
