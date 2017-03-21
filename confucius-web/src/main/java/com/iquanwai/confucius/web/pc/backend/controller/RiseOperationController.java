package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.backend.OperationManagementService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.pc.backend.dto.DiscussDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.ProblemCatalogDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.ProblemListDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by justin on 17/3/16.
 * RISE运营相关的接口
 */
@RestController
@RequestMapping("/pc/operation")
public class RiseOperationController {
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private OperationManagementService operationManagementService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private PracticeService practiceService;

    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final int APPLICATION_SUBMIT_SIZE = 20;

    @RequestMapping("/application/submit/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadApplicationSubmit(PCLoginUser loginUser,
                                                                    @PathVariable Integer applicationId,
                                                                    @ModelAttribute Page page) {
        page.setPageSize(APPLICATION_SUBMIT_SIZE);
        List<ApplicationSubmit> applicationSubmitList = operationManagementService.loadApplicationSubmit(applicationId, page);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("应用训练提交")
                .action("加载应用训练提交")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);

        return WebUtils.result(applicationSubmitList);
    }


    @RequestMapping("/hot/warmup")
    public ResponseEntity<Map<String, Object>> getHotPracticeDiscuss(PCLoginUser loginUser) {
        List<WarmupPractice> warmupPractices = operationManagementService.getLastTwoDayActivePractice();

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("理解训练讨论区")
                .action("加载最热的理解训练");
        operationLogService.log(operationLog);

        return WebUtils.result(warmupPractices);
    }

    @RequestMapping("/warmup/{practiceId}")
    public ResponseEntity<Map<String, Object>> getPracticeDiscuss(PCLoginUser loginUser,
                                                                    @PathVariable Integer practiceId) {
        WarmupPractice warmupPractice = operationManagementService.getWarmupPractice(practiceId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("理解训练讨论区")
                .action("加载理解训练讨论")
                .memo(practiceId.toString());
        operationLogService.log(operationLog);

        return WebUtils.result(warmupPractice);
    }


    @RequestMapping(value = "/reply/discuss", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> replyDiscuss(PCLoginUser loginUser,
                                                                  @RequestBody DiscussDto discussDto) {
        if(discussDto.getComment()==null || discussDto.getComment().length()>300){
            LOGGER.error("{} 理解训练讨论字数过长", loginUser.getOpenId());
            return WebUtils.result("您提交的讨论字数过长");
        }

        operationManagementService.discuss(loginUser.getOpenId(), discussDto.getWarmupPracticeId(),
                discussDto.getComment(), discussDto.getRepliedId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("理解训练")
                .action("回复讨论")
                .memo(discussDto.getWarmupPracticeId().toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/highlight/discuss/{discussId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> highlightDiscuss(PCLoginUser loginUser,
                                                         @PathVariable Integer discussId) {

        operationManagementService.highlightDiscuss(discussId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("理解训练")
                .action("加精讨论")
                .memo(discussId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/highlight/applicationSubmit/{practiceId}/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> highlightApplicationSubmit(PCLoginUser loginUser,
                                                         @PathVariable Integer practiceId,
                                                         @PathVariable Integer submitId) {

        operationManagementService.highlightApplicationSubmit(practiceId, submitId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("应用训练")
                .action("加精优秀的作业")
                .memo(submitId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }


    @RequestMapping("/problem/list")
    public ResponseEntity<Map<String, Object>> loadProblems(PCLoginUser pcLoginUser) {
        OperationLog operationLog = OperationLog.create().openid(pcLoginUser == null ? null : pcLoginUser.getOpenId())
                .module("内容运营")
                .function("应用训练")
                .action("获取问题列表");
        operationLogService.log(operationLog);

        List<Problem> problems = problemService.loadProblems();
        List<ProblemCatalog> catalogs = problemService.loadAllCatalog();
        List<ProblemCatalogDto> result = catalogs.stream().map(item -> {
            ProblemCatalogDto dto = new ProblemCatalogDto();
            List<ProblemListDto> collect = problems.stream().filter(problem -> Objects.equals(problem.getCatalogId(), item.getId())).map(problem -> {
                ProblemListDto problemList = new ProblemListDto();
                problemList.setId(problem.getId());
                problemList.setProblem(problem.getProblem());
                return problemList;
            }).collect(Collectors.toList());
            dto.setProblems(collect);
            dto.setName(item.getName());
            return dto;
        }).collect(Collectors.toList());
        return WebUtils.result(result);
    }


    /**
     * 碎片化总任务列表加载
     * @param problemId 问题id
     * @param pcLoginUser 登陆人
     */
    @RequestMapping("/homework/{problemId}")
    public ResponseEntity<Map<String, Object>> getProblemHomeworkList(@PathVariable Integer problemId, PCLoginUser pcLoginUser) {
        Assert.notNull(pcLoginUser, "用户信息能不能为空");
        List<ApplicationPractice> applicationPractices = practiceService.loadApplicationByProblemId(problemId);
        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("总任务列表加载")
                .memo(problemId+"");
        operationLogService.log(operationLog);

        return WebUtils.result(applicationPractices);
    }
}
