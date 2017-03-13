package com.iquanwai.confucius.web.pc.fragmentation.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/1/3.
 */
@Data
public class RiseWorkItemDto {
    private Integer type;
    private String title;
    private Integer score;
    private Boolean unlocked;
    private Integer status;

    private Integer planId;
    private Integer workId;
    private Integer submitId;
}
