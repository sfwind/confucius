package com.iquanwai.confucius.web.pc.backend.dto;

import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
import lombok.Data;

import java.util.List;

@Data
public class KnowledgeImportDto {

    private Integer id;
    private String problem;
    private List<ProblemSchedule> schedules;

}
