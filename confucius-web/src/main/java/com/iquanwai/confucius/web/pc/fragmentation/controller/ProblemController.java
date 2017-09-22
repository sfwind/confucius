package com.iquanwai.confucius.web.pc.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.backend.ProblemService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemCatalog;
import com.iquanwai.confucius.web.pc.fragmentation.dto.ProblemCatalogDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.ProblemListDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2016/12/29.
 */
@RestController
@RequestMapping("/pc/fragment/problem")
public class ProblemController {
    @Autowired
    private PlanService planService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private OperationLogService operationLogService;

    /**
     * 加载问题列表
     */
    @RequestMapping(value = "/list")
    public ResponseEntity<Map<String, Object>> loadProblemList(PCLoginUser pcLoginUser) {
//        Assert.notNull(pcLoginUser, "用户不能为空");
        // 兼容修改：不需要用户信息，用没登录时也可以查看问题列表
        OperationLog operationLog = OperationLog.create().openid(pcLoginUser != null ? pcLoginUser.getOpenId() : null)
                .module("训练")
                .function("碎片化")
                .action("获取问题列表");
        operationLogService.log(operationLog);

        List<ImprovementPlan> plans = pcLoginUser != null ? planService.loadUserPlans(pcLoginUser.getOpenId()) : Lists.newArrayList();
        List<Problem> problems = problemService.loadProblems();

        List<ProblemCatalog> catalogs = problemService.loadAllCatalog();
        List<ProblemCatalogDto> result = catalogs.stream().map(item -> {
            ProblemCatalogDto dto = new ProblemCatalogDto();
            List<ProblemListDto> collect = problems.stream().filter(problem -> Objects.equals(problem.getCatalogId(), item.getId())).map(problem -> {
                ProblemListDto problemListDto = new ProblemListDto();
                problemListDto.setId(problem.getId());
                problemListDto.setProblem(problem.getProblem());
                problemListDto.setDel(problem.getDel());
                problemListDto.setTrial(problem.getTrial());
                // 查询用户该小课的计划
                plans.forEach(plan -> {
                    if (plan.getProblemId() == problem.getId()) {
                        problemListDto.setStatus(plan.getStatus());
                    }
                });
                problemListDto.setStatus(problemListDto.getStatus() == null ? -1 : problemListDto.getStatus());
                return problemListDto;
            }).filter(problemListDto -> {
                //未选且已下架的小课从列表中删除
                return !(problemListDto.getDel() && problemListDto.getStatus() == -1);
            }).filter(problemListDto -> !(problemListDto.getTrial() && problemListDto.getStatus() == -1))
                    .collect(Collectors.toList());
            dto.setProblems(collect);
            dto.setName(item.getName());
            return dto;
        }).collect(Collectors.toList());
        return WebUtils.result(result);
    }
}
