package com.iquanwai.confucius.web.pc.fragmentation.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/1/3.
 */
@Data
public class RiseWorkListDto {
    private List<RiseWorkItemDto> challengeWorkList;
    private List<RiseWorkItemDto> applicationWorkList;
}
