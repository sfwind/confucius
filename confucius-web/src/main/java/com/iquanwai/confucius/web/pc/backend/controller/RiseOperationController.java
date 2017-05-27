package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.backend.OperationManagementService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.pc.backend.dto.DiscussDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.ProblemCatalogDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.ProblemListDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;
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
    @Autowired
    private MessageService messageService;

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
            Boolean isComment = operationManagementService.isComment(applicationSubmit.getId(), loginUser.getOpenId());
            applicationSubmit.setComment(isComment?1:0);
        });

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("应用练习提交")
                .action("加载应用练习提交")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);

        return WebUtils.result(applicationSubmitList);
    }


    @RequestMapping("/hot/warmup")
    public ResponseEntity<Map<String, Object>> getHotPracticeDiscuss(PCLoginUser loginUser) {
        List<WarmupPractice> warmupPractices = operationManagementService.getLastTwoDayActivePractice();

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习讨论区")
                .action("加载最热的巩固练习");
        operationLogService.log(operationLog);

        return WebUtils.result(warmupPractices);
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

    @RequestMapping("/warmup/load/{practiceId}")
    public ResponseEntity<Map<String, Object>> getPracticeDiscuss(PCLoginUser loginUser,
                                                                  @PathVariable Integer practiceId) {
        WarmupPractice warmupPractice = operationManagementService.getWarmupPractice(practiceId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习讨论区")
                .action("加载巩固练习讨论")
                .memo(practiceId.toString());
        operationLogService.log(operationLog);

        return WebUtils.result(warmupPractice);
    }

    /**
     * 删除助教的巩固练习评论
     */
    @RequestMapping("/warmup/discuss/del/{discussId}")
    public ResponseEntity<Map<String, Object>> deleteWarmupDiscuss(PCLoginUser loginUser, @PathVariable Integer discussId) {
        Pair<Integer, String> result = operationManagementService.deleteAsstWarmupDiscuss(discussId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习练习区")
                .action("删除巩固练习评论")
                .memo(result.getRight());
        operationLogService.log(operationLog);
        if(result.getLeft() == 1) {
            return WebUtils.success();
        } else if(result.getLeft() == 0) {
            return WebUtils.error(201, "抱歉，暂时不能删除非助教评论");
        } else {
            return WebUtils.error("系统异常");
        }
    }


    @RequestMapping(value = "/reply/discuss", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> replyDiscuss(PCLoginUser loginUser,
                                                            @RequestBody DiscussDto discussDto) {
        if (discussDto.getComment() == null || discussDto.getComment().length() > 300) {
            LOGGER.error("{} 巩固练习讨论字数过长", loginUser.getOpenId());
            return WebUtils.result("您提交的讨论字数过长");
        }

        operationManagementService.discuss(loginUser.getOpenId(), discussDto.getWarmupPracticeId(),
                discussDto.getComment(), discussDto.getRepliedId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习")
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

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser == null ? null : pcLoginUser.getOpenId())
                .module("内容运营")
                .function("应用练习")
                .action("获取问题列表");
        operationLogService.log(operationLog);
        return WebUtils.result(result);
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


    @RequestMapping(value="/warmup/save", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> savePractice(PCLoginUser loginUser,
                                                                  @RequestBody WarmupPractice warmupPractice) {

        operationManagementService.save(warmupPractice);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习编辑")
                .action("保存巩固练习")
                .memo(warmupPractice.getId()+"");
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
}
