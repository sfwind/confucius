package com.iquanwai.confucius.web.account.dto;

import com.iquanwai.confucius.biz.po.fragmentation.MemberType;
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
    private Boolean riseMember;
    private MemberType memberType;
}
