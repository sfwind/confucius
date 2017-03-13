package com.iquanwai.confucius.web.pc.fragmentation.dto;

import com.iquanwai.confucius.biz.po.fragmentation.LabelConfig;
import lombok.Data;

import java.util.List;

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

    private Boolean perfect;
    private Integer authorType;
    private Boolean isMine;
    private List<LabelConfig> labelList;
}
