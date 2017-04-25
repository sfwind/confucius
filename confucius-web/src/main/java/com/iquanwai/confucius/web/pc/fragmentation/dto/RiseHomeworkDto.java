package com.iquanwai.confucius.web.pc.fragmentation.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/1/13.
 */
@Data
public class RiseHomeworkDto {
    private Integer problemId;// 问题id
    private List<HomeworkDto> challengeList;
    private List<HomeworkDto> applicationList;
}
