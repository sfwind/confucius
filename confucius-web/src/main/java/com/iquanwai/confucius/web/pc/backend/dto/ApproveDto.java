package com.iquanwai.confucius.web.pc.backend.dto;

import com.iquanwai.confucius.biz.po.apply.InterviewRecord;
import lombok.Data;

/**
 * Created by nethunder on 2017/9/30.
 */
@Data
public class ApproveDto {
    private Integer id;
    private Double coupon;
    private InterviewRecord interviewRecord;
}
