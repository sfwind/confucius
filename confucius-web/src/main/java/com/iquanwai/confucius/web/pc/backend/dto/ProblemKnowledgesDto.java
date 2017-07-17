package com.iquanwai.confucius.web.pc.backend.dto;

import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
import lombok.Data;

import java.util.List;

@Data
public class ProblemKnowledgesDto {

    private List<Problem> problems;
    private List<ProblemSchedule> knowledges;

}
