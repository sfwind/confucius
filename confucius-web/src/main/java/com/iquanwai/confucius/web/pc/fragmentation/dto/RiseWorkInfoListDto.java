package com.iquanwai.confucius.web.pc.fragmentation.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/1/14.
 */
@Data
public class RiseWorkInfoListDto {
    private String title;
    private RiseWorkInfoDto mineWorkInfo;
    private List<RiseWorkInfoDto> otherWorkList;
}
