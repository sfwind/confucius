package com.iquanwai.confucius.web.pc.fragmentation.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2016/12/27.
 */
@Data
public class ProblemDto {
    private int id; // 问题id
    private Integer status; //问题状态（0-待解决，1-解决中，2-已解决）
    private Boolean pay; // 是否已付费
    private String problem; // 工作生活中遇到的问题
    private String pic; //头图
    private List<ChallengeDto> challengeList;
}
