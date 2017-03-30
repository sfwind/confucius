package com.iquanwai.confucius.web.account.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/2/7.
 */
@Data
public class PlanListDto {
    private String riseId;
    private List<PlanDto> runningPlans;
    private List<PlanDto> donePlans;
}
