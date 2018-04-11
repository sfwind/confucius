package com.iquanwai.confucius.biz.po.fragmentation.course;

import lombok.Data;

import java.util.Date;

@Data
public class MonthlyCampConfig extends CourseConfig {
    /**
     * 售卖中专项课结营日期
     */
    private Date closeDate;

}
