package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemCatalog;
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
     * 根据问题id获取问题
     * @param problemId 问题id
     * */
    Problem getProblem(Integer problemId);

    /**
     * 导入小课数据
     * */
    void saveProblem(Problem problem);
}
