package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by 三十文 on 2017/9/15
 */
@Data
public class MonthlyCampDtoGroup {

    private String groupId;
    private List<MonthlyCampDto> monthlyCampDtos;

}
