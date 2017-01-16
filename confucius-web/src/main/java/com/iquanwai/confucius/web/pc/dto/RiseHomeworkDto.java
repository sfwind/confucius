package com.iquanwai.confucius.web.pc.dto;

import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
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
