package com.iquanwai.confucius.web.pc.fragmentation.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/1/14.
 */
@Data
public class RiseWorkInfoDto {
    private String title;
    private String upName;
    private String upTime;
    private String headPic;
    private String content;
    private Integer voteCount;
    private Integer commentCount;
    private Integer submitId;
    private Integer type;
}
