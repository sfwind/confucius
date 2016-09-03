package com.iquanwai.confucius.biz.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 16/8/29.
 */
@Data
public class QuanwaiClass {
    private int id;
    private Date openTime; //开课时间
    private Date closeTime; //结业时间
    private Integer courseId; //课程id
    private Integer progress; //进度，当前课程进行到的章节id
    private Integer limit; //班级人数上限
    private String weixinGroup; //微信群二维码的链接
}
