package com.iquanwai.confucius.web.pc.asst.controller;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.asst.AssistantCoachService;
import com.iquanwai.confucius.biz.domain.backend.OperationManagementService;
import com.iquanwai.confucius.biz.domain.backend.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.RiseWorkInfoDto;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemCatalog;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPractice;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.pc.backend.dto.DiscussDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.ProblemCatalogDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.ProblemListDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RefreshListDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

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
    private OperationManagementService operationManagementService;
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

    @RequestMapping(value = "/application/nickname/{problemId}/{nickName}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getUnderCommentApplicationByNickName(PCLoginUser pcLoginUser, @PathVariable Integer problemId, @PathVariable String nickName) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        List<RiseWorkInfoDto> riseWorkInfoDtos = assistantCoachService.getUnderCommentApplicationsByNickName(problemId, nickName);
        return WebUtils.result(riseWorkInfoDtos);
    }

    @RequestMapping(value = "/application/memberid/{problemId}/{memberId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getUnderCommentApplicationByMemberId(PCLoginUser pcLoginUser, @PathVariable Integer problemId, @PathVariable String memberId) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        List<RiseWorkInfoDto> riseWorkInfoDtos = assistantCoachService.getUnderCommentApplicationsByMemberId(problemId, memberId);
        return WebUtils.result(riseWorkInfoDtos);
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
        Pair<Integer, Integer> counts = assistantCoachService.getCommentCount(pcLoginUser.getProfileId());
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

    @RequestMapping("/application/problem/list")
    public ResponseEntity<Map<String, Object>> loadApplicationProblems(PCLoginUser pcLoginUser) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        List<Problem> problems = problemService.loadProblems();
        List<ProblemCatalog> catalogs = problemService.loadAllCatalogs();
        Map<Integer, Integer> underCommentMap = assistantCoachService.getUnderCommentApplicationCount();
        List<ProblemCatalogDto> result = catalogs.stream().map(item -> {
            ProblemCatalogDto dto = new ProblemCatalogDto();
            List<ProblemListDto> collect = problems.stream().filter(problem -> !problem.getDel())
                    .filter(problem -> Objects.equals(problem.getCatalogId(), item.getId())).map(problem -> {
                        ProblemListDto problemList = new ProblemListDto();
                        problemList.setId(problem.getId());
                        problemList.setProblem(problem.getProblem());
                        problemList.setUnderCommentCount(underCommentMap.get(problem.getId()));
                        return problemList;
                    }).collect(Collectors.toList());
            dto.setProblems(collect);
            dto.setName(item.getName());
            return dto;
        }).collect(Collectors.toList());

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("助教后台")
                .function("应用练习评论")
                .action("获取问题列表");
        operationLogService.log(operationLog);
        return WebUtils.result(result);
    }

    @RequestMapping("/subject/problem/list")
    public ResponseEntity<Map<String, Object>> loadSubjectArticleProblems(PCLoginUser pcLoginUser) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        List<Problem> problems = problemService.loadProblems();
        List<ProblemCatalog> catalogs = problemService.loadAllCatalogs();
        Map<Integer, Integer> underCommentMap = assistantCoachService.getUnderCommentSubjectArticleCount();
        List<ProblemCatalogDto> result = catalogs.stream().map(item -> {
            ProblemCatalogDto dto = new ProblemCatalogDto();
            List<ProblemListDto> collect = problems.stream().filter(problem -> !problem.getDel())
                    .filter(problem -> Objects.equals(problem.getCatalogId(), item.getId())).map(problem -> {
                        ProblemListDto problemList = new ProblemListDto();
                        problemList.setId(problem.getId());
                        problemList.setProblem(problem.getProblem());
                        problemList.setUnderCommentCount(underCommentMap.get(problem.getId()));
                        return problemList;
                    }).collect(Collectors.toList());
            dto.setProblems(collect);
            dto.setName(item.getName());
            return dto;
        }).collect(Collectors.toList());

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("助教后台")
                .function("小课分享评论")
                .action("获取问题列表");
        operationLogService.log(operationLog);
        return WebUtils.result(result);
    }

    @RequestMapping("/commented/submit")
    public ResponseEntity<Map<String, Object>> getCommentedSubmit(PCLoginUser pcLoginUser) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        List<RiseWorkInfoDto> riseWorkInfoDtos = assistantCoachService.getCommentedSubmit(pcLoginUser.getProfileId());

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("助教后台")
                .function("评论")
                .action("获取已评论文章");
        operationLogService.log(operationLog);

        return WebUtils.result(riseWorkInfoDtos);
    }

    @RequestMapping("/hot/warmup")
    public ResponseEntity<Map<String, Object>> getHotPracticeDiscuss(PCLoginUser loginUser, @ModelAttribute Page page) {
        //每页50道题目
        page.setPageSize(50);
        List<WarmupPractice> warmupPractices = operationManagementService.getLastSixtyDayActivePractice(page);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习讨论区")
                .action("加载最热的巩固练习");
        operationLogService.log(operationLog);

        RefreshListDto<WarmupPractice> refreshListDto = new RefreshListDto<>();
        refreshListDto.setEnd(page.isLastPage());
        refreshListDto.setList(warmupPractices);

        return WebUtils.result(refreshListDto);
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

    @RequestMapping(value = "/reply/discuss", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> replyDiscuss(PCLoginUser loginUser,
                                                            @RequestBody DiscussDto discussDto) {
        if (discussDto.getComment() == null || discussDto.getComment().length() > 1000) {
            return WebUtils.result("您提交的讨论字数过长");
        }

        operationManagementService.discuss(loginUser.getOpenId(), loginUser.getProfileId(),
                discussDto.getWarmupPracticeId(),
                discussDto.getComment(), discussDto.getRepliedId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习")
                .action("回复讨论")
                .memo(discussDto.getWarmupPracticeId().toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

}
