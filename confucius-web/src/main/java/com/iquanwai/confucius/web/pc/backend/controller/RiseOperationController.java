package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.backend.OperationManagementService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.po.systematism.Choice;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.pc.backend.dto.ProblemKnowledgesDto;
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
    // 消息中心消息，显示为系统头像
    private static final String SYSTEM_MESSAGE = "AUTO";

    @RequestMapping("/application/submit/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadApplicationSubmit(PCLoginUser loginUser,
                                                                     @PathVariable Integer applicationId,
                                                                     @ModelAttribute Page page) {
        page.setPageSize(APPLICATION_SUBMIT_SIZE);
        List<ApplicationSubmit> applicationSubmitList = operationManagementService.loadApplicationSubmit(applicationId, page);

        applicationSubmitList.stream().forEach(applicationSubmit -> {
            Boolean isComment = operationManagementService.isComment(applicationSubmit.getId(), loginUser.getProfileId());
            applicationSubmit.setComment(isComment ? 1 : 0);
        });

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("应用练习提交")
                .action("加载应用练习提交")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);

        return WebUtils.result(applicationSubmitList);
    }

    @RequestMapping("/warmup/list/{problemId}")
    public ResponseEntity<Map<String, Object>> getProblemWarmupPractice(PCLoginUser loginUser,
                                                                        @PathVariable Integer problemId) {
        List<WarmupPractice> warmupPractices = operationManagementService.getPracticeByProblemId(problemId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习编辑")
                .action("加载小课的巩固练习");
        operationLogService.log(operationLog);

        return WebUtils.result(warmupPractices);
    }

    @RequestMapping(value = "/highlight/discuss/{discussId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> highlightDiscuss(PCLoginUser loginUser,
                                                                @PathVariable Integer discussId) {

        operationManagementService.highlightDiscuss(discussId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习")
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
                .function("应用练习")
                .action("加精优秀的作业")
                .memo(submitId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping("/problem/list")
    public ResponseEntity<Map<String, Object>> loadProblems(PCLoginUser pcLoginUser) {

        List<Problem> problems = problemService.loadProblems();
        List<ProblemCatalog> catalogs = problemService.loadAllCatalog();
        List<ProblemCatalogDto> result = catalogs.stream().map(item -> {
            ProblemCatalogDto dto = new ProblemCatalogDto();
            List<ProblemListDto> collect = problems.stream().filter(problem -> !problem.getDel())
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

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser == null ? null : pcLoginUser.getOpenId())
                .module("内容运营")
                .function("应用练习")
                .action("获取问题列表");
        operationLogService.log(operationLog);
        return WebUtils.result(result);
    }

    /**
     * 删除助教的巩固练习评论
     */
    @RequestMapping("/warmup/discuss/del/{discussId}")
    public ResponseEntity<Map<String, Object>> deleteWarmupDiscuss(PCLoginUser loginUser, @PathVariable Integer discussId) {
        Integer result = operationManagementService.deleteAsstWarmupDiscuss(discussId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习练习区")
                .action("删除巩固练习评论")
                .memo(discussId.toString());
        operationLogService.log(operationLog);
        if(result == 1) {
            return WebUtils.success();
        } else if(result == 0) {
            return WebUtils.error(201, "抱歉，暂时不能删除非助教评论");
        } else {
            return WebUtils.error("系统异常");
        }
    }

    /**
     * 碎片化总任务列表加载
     *
     * @param problemId   问题id
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
                .memo(problemId + "");
        operationLogService.log(operationLog);

        return WebUtils.result(applicationPractices);
    }


    @RequestMapping(value = "/warmup/save", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> savePractice(PCLoginUser loginUser,
                                                            @RequestBody WarmupPractice warmupPractice) {

        operationManagementService.save(warmupPractice);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习编辑")
                .action("保存巩固练习")
                .memo(warmupPractice.getId() + "");
        operationLogService.log(operationLog);

        return WebUtils.success();
    }

    @RequestMapping("/warmup/next/{problemId}/{practiceId}")
    public ResponseEntity<Map<String, Object>> getNextPractice(PCLoginUser loginUser,
                                                               @PathVariable Integer problemId,
                                                               @PathVariable Integer practiceId) {

        WarmupPractice warmupPractice = operationManagementService.getNextPractice(problemId, practiceId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习编辑")
                .action("加载下一巩固练习");
        operationLogService.log(operationLog);

        return WebUtils.result(warmupPractice);
    }

    @RequestMapping(value = "/warmup/load/knowledges", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadKnowledgesGroupByProblem(PCLoginUser loginUser) {
        Assert.notNull(loginUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("巩固练习更改").function("巩固练习新增").action("加载问题与知识点");
        operationLogService.log(operationLog);
        List<Problem> problems = problemService.loadProblems();
        List<ProblemSchedule> knowledges = operationManagementService.loadKnowledgesGroupByProblem();
        if(problems != null && knowledges != null) {
            ProblemKnowledgesDto dto = new ProblemKnowledgesDto();
            dto.setProblems(problems);
            dto.setKnowledges(knowledges);
            return WebUtils.result(dto);
        }
        return WebUtils.error("未找到小课与知识点关联信息");
    }

    @RequestMapping(value = "/warmup/insert/practice", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> insertWarmupPractice(PCLoginUser loginUser, @RequestBody WarmupPractice warmupPractice) {
        Assert.notNull(loginUser, "用户不能为空");
        List<WarmupChoice> warmupChoices = warmupPractice.getChoices();
        Integer knowledgeId = practiceService.insertWarmupPractice(warmupPractice);
        if(knowledgeId <= 0) {
            return WebUtils.error("巩固练习数据插入失败，请及时练习管理员");
        } else {
            Integer result = practiceService.insertWarmupChoice(knowledgeId, warmupChoices);
            if(result <= 0) {
                return WebUtils.error("选项数据插入失败，请及时练习管理员");
            }
        }
        return WebUtils.success();
    }

}
