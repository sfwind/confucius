package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.backend.OperationManagementService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.web.pc.backend.dto.SimpleProblem;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 2017/9/18.
 */
@RestController
@RequestMapping("/pc/operation")
public class ProblemImportController {
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private OperationManagementService operationManagementService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private PracticeService practiceService;

    private Logger LOGGER = LoggerFactory.getLogger(getClass());


    @RequestMapping("/problem/simple")
    public ResponseEntity<Map<String, Object>> getSimpleProblem(PCLoginUser loginUser) {


        List<SimpleProblem> simpleProblems = problemService.loadProblems().stream()
                .filter(problem -> !problem.getDel())
                .map(problem -> new SimpleProblem(problem.getId(), problem.getProblem()))
                .collect(Collectors.toList());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("选择小课")
                .action("加载小课");
        operationLogService.log(operationLog);

        return WebUtils.result(simpleProblems);
    }
}
