package com.iquanwai.confucius.web.pc.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nethunder on 2017/1/11.
 */
@Data
public class ImprovementPlanDto {
    private Integer status;// 状态
    private Integer id;// 计划任务id
}
