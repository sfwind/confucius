package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;

import java.util.List;

public interface KnowledgeService {

    Knowledge loadKnowledge(Integer knowledgeId);

    List<Knowledge> loadKnowledges(Integer problemId);

    Integer updateKnowledge(Knowledge knowledge, Integer problemId);

    List<Knowledge> queryAllKnowLedges();

    /**
     * 插入ProblemSchedule表
     * @param problemId
     * @return
     */
    int insertProblemScehdule(Integer problemId);

    List<ProblemSchedule> loadKnowledgesGroupByProblem();
}
