package com.iquanwai.confucius.web.pc.fragmentation.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/2/24.
 */
@Data
public class ProblemCatalogDto {
    private String name;
    private List<ProblemListDto> problems;
}
