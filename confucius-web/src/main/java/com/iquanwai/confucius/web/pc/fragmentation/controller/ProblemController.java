package com.iquanwai.confucius.web.pc.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.web.pc.dto.ProblemListDto;
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


    @RequestMapping("/curId")
    public ResponseEntity<Map<String,Object>> loadCurProblemId(PCLoginUser pcLoginUser){
        Assert.notNull(pcLoginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("获取用户当前在解决的问题Id")
                .memo("");
        operationLogService.log(operationLog);
        ImprovementPlan runningPlan = planService.getRunningPlan(pcLoginUser.getOpenId());
        if(runningPlan == null){
            // 没有正在进行的主题，选一个之前做过的
            List<ImprovementPlan> plans = planService.loadUserPlans(pcLoginUser.getOpenId());
            if(plans.isEmpty()){
                // 没有买过难题
                logger.error("{} has no active plan", pcLoginUser.getOpenId());
                return WebUtils.error(ErrorConstants.NOT_PAY_FRAGMENT,"没找到进行中的RISE训练");
            } else {
                // 购买过直接选最后一个
                ImprovementPlan plan = plans.get(plans.size()-1);
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
        OperationLog operationLog = OperationLog.create().openid(pcLoginUser==null?null:pcLoginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("获取问题列表")
                .memo("");
        operationLogService.log(operationLog);
        List<Problem> problems = problemService.loadProblems();
        List<ProblemListDto> result = Lists.newArrayList();
        problems.forEach(item -> {
            ProblemListDto dto = new ProblemListDto();
            dto.setId(item.getId());
            dto.setProblem(item.getProblem());
            result.add(dto);
        });
        return WebUtils.result(result);
    }
}
