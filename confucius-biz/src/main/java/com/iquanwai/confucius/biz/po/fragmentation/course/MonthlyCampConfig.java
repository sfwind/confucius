package com.iquanwai.confucius.biz.po.fragmentation.course;

import lombok.Data;

@Data
public class MonthlyCampConfig extends CourseConfig {

    public MonthlyCampConfig copy() {
        MonthlyCampConfig config = new MonthlyCampConfig();
        config.setActive(this.getActive());
        config.setCloseDate(this.getCloseDate());
        config.setOpenDate(this.getOpenDate());
        config.setPurchaseSwitch(this.getPurchaseSwitch());
        config.setSellingMonth(this.getSellingMonth());
        config.setSellingYear(this.getSellingYear());
        return config;
    }
}
