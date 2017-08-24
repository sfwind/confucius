package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;

import java.util.List;

public interface KnowledgeImportService {

    Problem loadEditableProblem();

    List<ProblemSchedule> loadEditableProblemSchedules();

    Knowledge loadKnowledge(Integer knowledgeId);

    int addNewChapter(Integer chapter);

    int addNewSection(Integer chapter, Integer section);
}
