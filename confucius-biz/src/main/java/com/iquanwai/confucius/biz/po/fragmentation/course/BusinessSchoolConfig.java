package com.iquanwai.confucius.biz.po.fragmentation.course;

import lombok.Data;

@Data
public class BusinessSchoolConfig extends CourseConfig {

    public BusinessSchoolConfig copy() {
        BusinessSchoolConfig config = new BusinessSchoolConfig();
        config.setActive(this.getActive());
        config.setOpenDate(this.getOpenDate());
        config.setPurchaseSwitch(this.getPurchaseSwitch());
        config.setSellingMonth(this.getSellingMonth());
        config.setSellingYear(this.getSellingYear());
        return config;
    }
}
