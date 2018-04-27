package com.iquanwai.confucius.biz.domain.backend.problem;

import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;

/**
 * 知识点章节
 */
public interface ScheduleService {
    /**
     * @param problemId
     * @param chapter
     * @param section
     * @return
     */
    ProblemSchedule loadProblemSchedule(Integer problemId,Integer chapter,Integer section);

}
