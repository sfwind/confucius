package com.iquanwai.confucius.web.customer.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/2/7.
 */
@Data
public class PlanListDto {
    private List<PlanDto> runningPlans;
    private List<PlanDto> donePlans;
}
