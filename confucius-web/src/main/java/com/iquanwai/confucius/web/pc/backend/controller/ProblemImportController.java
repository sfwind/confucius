package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.backend.ProblemService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
import com.iquanwai.confucius.web.pc.backend.dto.CatalogDto;
import com.iquanwai.confucius.web.pc.backend.dto.SimpleProblem;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 2017/9/18.
 */
@RestController
@RequestMapping("/pc/operation/problem")
public class ProblemImportController {
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private ProblemService problemService;


    @RequestMapping("/simple")
    public ResponseEntity<Map<String, Object>> getSimpleProblem(PCLoginUser loginUser) {
        List<SimpleProblem> simpleProblems = problemService.loadProblems().stream()
                .filter(problem -> !problem.getDel())
                .map(problem -> new SimpleProblem(problem.getId(), problem.getProblem()))
                .collect(Collectors.toList());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("选择课程")
                .action("加载所有课程");
        operationLogService.log(operationLog);

        return WebUtils.result(simpleProblems);
    }

    @RequestMapping("/load/{id}")
    public ResponseEntity<Map<String, Object>> getProblem(PCLoginUser loginUser,
                                                          @PathVariable Integer id) {
        Problem problem = problemService.getProblem(id);
        List<ProblemSchedule> schedules = problemService.loadProblemSchedules(id);
        problem.setSchedules(schedules);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("选择课程")
                .action("加载课程")
                .memo(id.toString());
        operationLogService.log(operationLog);

        return WebUtils.result(problem);
    }

    /**
     * 添加和更新课程功能
     *
     * @param loginUser
     * @param problem
     * @return
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> saveProblem(PCLoginUser loginUser,
                                                           @RequestBody Problem problem) {
        //save包含插入和更新操作
        int problemId = problemService.saveProblem(problem);
        //判断是否已经有复习Schedule,没有则需要添加
        if (!problemService.isHasReviewProblemSchedule(problemId)) {
            problemService.insertProblemScehdule(problemId);
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("选择课程")
                .action("保存课程");
        operationLogService.log(operationLog);

        return WebUtils.result(problemId);
    }

    @RequestMapping("/catalog/load")
    public ResponseEntity<Map<String, Object>> getCatalogs(PCLoginUser loginUser) {
        CatalogDto catalogDto = new CatalogDto();
        catalogDto.setCatalogs(problemService.loadAllCatalogs());
        catalogDto.setSubCatalogs(problemService.loadAllSubCatalogs());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("选择课程")
                .action("加载所有类别");
        operationLogService.log(operationLog);

        return WebUtils.result(catalogDto);
    }
}
