package com.iquanwai.confucius.web.pc.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.backend.ProblemService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.whitelist.WhiteListService;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemCatalog;
import com.iquanwai.confucius.web.pc.fragmentation.dto.ProblemCatalogDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.ProblemListDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
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

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WhiteListService whiteListService;

    private static final String TRIAL = "RISE_PROBLEM_TRIAL";


    @RequestMapping("/curId")
    public ResponseEntity<Map<String, Object>> loadCurProblemId(PCLoginUser pcLoginUser) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("获取用户当前在解决的问题Id");
        operationLogService.log(operationLog);
        ImprovementPlan runningPlan = planService.getRunningPlan(pcLoginUser.getOpenId());
        if (runningPlan == null) {
            // 没有正在进行的主题，选一个之前做过的
            List<ImprovementPlan> plans = planService.loadUserPlans(pcLoginUser.getOpenId());
            if (plans.isEmpty()) {
                // 没有买过难题
                logger.error("{} has no active plan", pcLoginUser.getOpenId());
                return WebUtils.error(ErrorConstants.NOT_PAY_FRAGMENT, "没找到进行中的小课");
            } else {
                // 购买过直接选最后一个
                ImprovementPlan plan = plans.get(plans.size() - 1);
                return WebUtils.result(plan.getProblemId());
            }
        } else {
            // 有正在进行的主题，直接返回id
            return WebUtils.result(runningPlan.getProblemId());
        }
    }


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
        //是否是天使用户
        boolean trialUser = pcLoginUser != null && whiteListService.isInWhiteList(TRIAL, pcLoginUser.getProfileId());

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
            }).filter(problemListDto -> {
                if (trialUser) {
                    return true;
                } else {
                    //非天使用户,把未选且试用的小课从列表中删除
                    return !(problemListDto.getTrial() && problemListDto.getStatus() == -1);
                }
            }).collect(Collectors.toList());
            dto.setProblems(collect);
            dto.setName(item.getName());
            return dto;
        }).collect(Collectors.toList());
        return WebUtils.result(result);
    }
}
