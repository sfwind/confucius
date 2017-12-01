package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemCatalog;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSubCatalog;

import java.util.List;

/**
 * Created by nethunder on 2016/12/28.
 */
public interface ProblemService {
    /**
     * 获取所有工作中的问题
     * */
    List<Problem> loadProblems();

    /**
     * 获取小课主分类
     * */
    List<ProblemCatalog> loadAllCatalogs();

    /**
     * 获取小课次级分类
     * */
    List<ProblemSubCatalog> loadAllSubCatalogs();

    /**
     * 获取小课课程表
     * */
    List<ProblemSchedule> loadProblemSchedules(Integer problemId);

    /**
     * 根据问题id获取问题
     * @param problemId 问题id
     * */
    Problem getProblem(Integer problemId);

    /**
     * 导入小课数据
     * */
    int saveProblem(Problem problem);


    /**
     * 插入ProblemSchedule表
     * @param problemId
     * @return
     */
    int insertProblemScehdule(Integer problemId);

    /**
     * 判断是否有复习ProblemSchedule
     * @param problemId
     * @return
     */
    boolean isHasReviewProblemSchedule(Integer problemId);

}
