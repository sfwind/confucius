package com.iquanwai.confucius.web.pc.asst.controller;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.asst.AssistantCoachService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.RiseWorkInfoDto;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemCatalog;
import com.iquanwai.confucius.web.pc.fragmentation.dto.ProblemCatalogDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.ProblemListDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by justin on 17/4/27.
 */
@RestController
@RequestMapping("/pc/asst")
public class AssistantCoachController {
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AssistantCoachService assistantCoachService;
    @Autowired
    private ProblemService problemService;

    @RequestMapping("/application/{problemId}")
    public ResponseEntity<Map<String, Object>> getUnderCommentApplication(PCLoginUser pcLoginUser,
                                                                     @PathVariable Integer problemId) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        List<RiseWorkInfoDto> applicationSubmit = assistantCoachService.getUnderCommentApplications(problemId);

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("助教后台")
                .function("应用练习")
                .action("获取待评论的应用练习");
        operationLogService.log(operationLog);

        return WebUtils.result(applicationSubmit);
    }

    @RequestMapping("/subject/{problemId}")
    public ResponseEntity<Map<String, Object>> getUnderCommentSubject(PCLoginUser pcLoginUser,
                                                                     @PathVariable Integer problemId) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        List<RiseWorkInfoDto> applicationSubmit = assistantCoachService.getUnderCommentArticles(problemId);

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("助教后台")
                .function("小课论坛")
                .action("获取待评论的小课分享");
        operationLogService.log(operationLog);

        return WebUtils.result(applicationSubmit);
    }

    @RequestMapping("/comment/count")
    public ResponseEntity<Map<String, Object>> getCommentCount(PCLoginUser pcLoginUser) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        Pair<Integer, Integer> counts = assistantCoachService.getCommentCount(pcLoginUser.getOpenId());
        Map<String, Integer> countMap = Maps.newHashMap();
        countMap.put("totalComment", counts.getRight());
        countMap.put("todayComment", counts.getLeft());

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("助教后台")
                .function("小课论坛")
                .action("获取待评论的小课分享");
        operationLogService.log(operationLog);

        return WebUtils.result(countMap);
    }

    @RequestMapping("/problem/list")
    public ResponseEntity<Map<String, Object>> loadProblem(PCLoginUser pcLoginUser) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        List<Problem> problems = problemService.loadProblems();
        List<ProblemCatalog> catalogs = problemService.loadAllCatalog();
        List<ProblemCatalogDto> result = catalogs.stream().map(item -> {
            ProblemCatalogDto dto = new ProblemCatalogDto();
            List<ProblemListDto> collect = problems.stream().filter(problem->!problem.getDel())
                    .filter(problem -> Objects.equals(problem.getCatalogId(), item.getId())).map(problem -> {
                        ProblemListDto problemList = new ProblemListDto();
                        problemList.setId(problem.getId());
                        problemList.setProblem(problem.getProblem());
                        return problemList;
                    }).collect(Collectors.toList());
            dto.setProblems(collect);
            dto.setName(item.getName());
            return dto;
        }).collect(Collectors.toList());

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("助教后台")
                .function("练习评论")
                .action("获取问题列表");
        operationLogService.log(operationLog);
        return WebUtils.result(result);
    }

}
