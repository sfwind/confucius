package com.iquanwai.confucius.biz.domain.fragmentation.plan;

import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemCatalog;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemList;

import java.util.List;

/**
 * Created by nethunder on 2016/12/28.
 */
public interface ProblemService {
    /**
     * 获取所有工作中的问题
     * */
    List<Problem> loadProblems();

    List<ProblemCatalog> loadAllCatalog();

    /**
     * 获取学员的问题
     * @param openid 学员id
     * */
    List<ProblemList> loadProblems(String openid);

    /**
     * 根据问题id获取问题
     * @param problemId 问题id
     * */
    Problem getProblem(Integer problemId);
}
