package com.iquanwai.confucius.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class Chapter {
    private int id;
    private Integer courseId; //课程id
    private String name;  //章节名称
    private Integer type; //章节类型（1-挑战，2-作业，3-讨论，4-休息）
    private Integer sequence; //课程内顺序
    private Integer week; //章节所在的周序号
    private Integer startDay; //开始于第几天
    private Integer endDay; //结束于第几天
    private boolean unlock; //是否解锁
    private boolean complete; //是否完成
    private String icon; //icon链接
}
