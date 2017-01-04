package com.iquanwai.confucius.web.pc.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2016/12/29.
 */
@Data
public class FragmentPageDto {
    private List<ProblemDto> problemList;
    private Integer doingId;
}
