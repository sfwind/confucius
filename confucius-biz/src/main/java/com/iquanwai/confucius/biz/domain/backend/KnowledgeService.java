package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;

import java.util.List;

public interface KnowledgeService {

    Knowledge loadKnowledge(Integer knowledgeId);

    List<Knowledge> loadKnowledges(Integer problemId);

    Integer updateKnowledge(Knowledge knowledge, Integer problemId);

    List<Knowledge> queryAllKnowLedges();
}
