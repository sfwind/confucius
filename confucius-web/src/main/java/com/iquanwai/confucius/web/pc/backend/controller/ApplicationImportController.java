package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.ApplicationService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.RiseWorkInfoDto;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationPractice;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.HtmlRegexpUtil;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.pc.backend.dto.ApplicationDto;
import com.iquanwai.confucius.web.pc.backend.dto.RefreshListDto;
import com.iquanwai.confucius.web.pc.backend.dto.RiseWorkEditDto;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2017/1/14.
 */
@RestController
@RequestMapping("/pc/operation/application")
public class ApplicationImportController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PlanService planService;

    /**
     * 获取应用练习标题
     *
     * @param loginUser     登陆人
     * @param applicationId 应用练习Id
     */
    @RequestMapping("/title/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadApplicationTitle(PCLoginUser loginUser, @PathVariable Integer applicationId) {
        Assert.notNull(applicationId, "应用练习id不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("加载应用任务标题")
                .memo(applicationId + "");
        operationLogService.log(operationLog);
        ApplicationPractice applicationPractice = applicationService.loadApplicationPractice(applicationId);
        return WebUtils.result(applicationPractice.getTopic());
    }

    /**
     * 获取应用练习
     *
     * @param loginUser     登陆人
     * @param planId        计划ID
     * @param applicationId 应用练习ID
     * @return
     */
    @RequestMapping("/mine/{planId}/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadMineApplication(PCLoginUser loginUser,
                                                                   @PathVariable("planId") Integer planId,
                                                                   @PathVariable("applicationId") Integer applicationId) {
        Assert.notNull(loginUser, "用户信息不能为空");
        Assert.notNull(planId, "计划id不能为空");
        Assert.notNull(applicationId, "应用练习id不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("加载自己的应用任务")
                .memo(planId + ":" + applicationId);
        operationLogService.log(operationLog);
        // 先检查该用户有没有买这个作业
        List<ImprovementPlan> userPlans = planService.loadUserPlans(loginUser.getProfileId());
        // 看看这个id在不在
        Optional<ImprovementPlan> plan = userPlans.stream().filter(item -> Objects.equals(item.getId(), planId)).findFirst();
        if (plan.isPresent()) {
            ApplicationSubmit applicationSubmit = applicationService.loadMineApplicationPractice(planId, applicationId,
                    loginUser.getProfileId(), loginUser.getOpenId(),false);
            RiseWorkEditDto dto = new RiseWorkEditDto();
            dto.setSubmitId(applicationSubmit.getId());
            dto.setTitle(applicationSubmit.getTopic());
            dto.setContent(applicationSubmit.getContent());
            dto.setDescription(applicationSubmit.getDescription());
            dto.setModuleId(Constants.PictureType.APPLICATION);
//            List<Picture> pictureList = pictureService.loadPicture(Constants.PictureType.APPLICATION, dto.getSubmitId());
//            dto.setPicList(pictureList
//                    .stream()
//                    .map(item -> pictureService.getModulePrefix(Constants.PictureType.APPLICATION) + item.getRealName())
//                    .collect(Collectors.toList()));
            dto.setRequestCommentCount(practiceService.hasRequestComment(planId));
            dto.setRequest(applicationSubmit.getRequestFeedback());
            return WebUtils.result(dto);
        } else {
            logger.error("用户:{},没有该训练计划:{}，应用练习:{}", loginUser.getProfileId(), plan, applicationId);
            return WebUtils.error(ErrorConstants.NOT_PAY_PROBLEM, "未购买的问题");
        }

    }

    /**
     * 应用列表页加载自己的任务
     *
     * @param loginUser     登陆人
     * @param planId        计划Id
     * @param applicationId 应用练习ID
     */
    @RequestMapping("/list/mine/{planId}/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadMineApplicationList(PCLoginUser loginUser, @PathVariable("planId") Integer planId, @PathVariable("applicationId") Integer applicationId) {
        Assert.notNull(loginUser, "用户信息不能为空");
        Assert.notNull(planId, "计划不能为空");
        Assert.notNull(applicationId, "应用练习不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("应用任务列表加载自己的应用任务")
                .memo(applicationId + "");
        operationLogService.log(operationLog);
        ApplicationSubmit applicationSubmit = applicationService.loadMineApplicationPractice(planId, applicationId,
                loginUser.getProfileId(), loginUser.getOpenId(),false);
        RiseWorkInfoDto dto = new RiseWorkInfoDto();
        dto.setSubmitId(applicationSubmit.getId());
        dto.setTitle(applicationSubmit.getTopic());
        if(applicationSubmit.getContent() != null) {
            applicationSubmit.setContent(HtmlRegexpUtil.filterHtml(applicationSubmit.getContent()));
            dto.setContent(applicationSubmit.getContent().length() > 180 ?
                    applicationSubmit.getContent().substring(0, 180) + "......" :
                    applicationSubmit.getContent());
        }
        dto.setHeadPic(loginUser.getWeixin().getHeadimgUrl());
        dto.setType(Constants.PracticeType.APPLICATION);
        dto.setUpName(loginUser.getWeixin().getWeixinName());
        dto.setUpTime(DateUtils.parseDateToFormat5(applicationSubmit.getPublishTime()));
        dto.setVoteCount(practiceService.loadHomeworkVotesCount(Constants.VoteType.APPLICATION, applicationSubmit.getId()));
        dto.setRequestCommentCount(practiceService.hasRequestComment(planId));
        dto.setRequest(applicationSubmit.getRequestFeedback());
        return WebUtils.result(dto);
    }

    /**
     * 应用任务列表页加载他人的任务信息
     *
     * @param loginUser     登陆人
     * @param applicationId 应用任务Id
     */
    @RequestMapping("/list/other/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadOtherApplicationList(PCLoginUser loginUser,
                                                                        @PathVariable Integer applicationId,
                                                                        @ModelAttribute Page page) {
        Assert.notNull(loginUser, "用户信息不能为空");
        Assert.notNull(applicationId, "应用练习不能为空");
        page.setPageSize(20);
        // 该计划的应用练习是否提交
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("应用任务列表加载他人的应用任务")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);
        List<RiseWorkInfoDto> submits = applicationService.loadApplicationSubmitList(applicationId).stream()
                .filter(item -> !item.getOpenid().equals(loginUser.getOpenId())).map(item -> {
                    RiseWorkInfoDto dto = new RiseWorkInfoDto();
                    item.setContent(HtmlRegexpUtil.filterHtml(item.getContent()));
                    dto.setContent(item.getContent().length() > 180 ?
                            item.getContent().substring(0, 180) + "......" :
                            item.getContent());
                    dto.setVoteCount(practiceService.loadHomeworkVotesCount(Constants.VoteType.APPLICATION, item.getId()));
                    dto.setUpTime(DateUtils.parseDateToFormat5(item.getPublishTime()));
                    dto.setType(Constants.PracticeType.APPLICATION);
                    dto.setPublishTime(item.getPublishTime());
                    dto.setSubmitId(item.getId());
                    dto.setPriority(item.getPriority());
                    Profile profile = accountService.getProfile(item.getOpenid(), false);
                    if(profile != null) {
                        dto.setUpName(profile.getNickname());
                        dto.setHeadPic(profile.getHeadimgurl());
                        dto.setRole(profile.getRole());
                        dto.setSignature(profile.getSignature());
                    }
                    dto.setCommentCount(practiceService.commentCount(Constants.CommentModule.APPLICATION, item.getId()));
                    return dto;
                }).sorted((left, right) -> {
                    //按发布时间排序
                    try {
                        return (int) ((right.getPublishTime().getTime() - left.getPublishTime().getTime()) / 1000);
                    } catch(Exception e) {
                        logger.error("应用任务文章排序异常", e);
                        return 0;
                    }
                }).collect(Collectors.toList());

        RefreshListDto<RiseWorkInfoDto> refreshListDto = new RefreshListDto<>();
        //区分精华和普通文章
        List<RiseWorkInfoDto> superbSubmit = submits.stream().filter(submit -> submit.getPriority() == 1)
                .collect(Collectors.toList());
        //普通文章分页
        List<RiseWorkInfoDto> normalSubmit = submits.stream().filter(submit -> submit.getPriority() == 0).collect(Collectors.toList());
        page.setTotal(normalSubmit.size());
        normalSubmit = normalSubmit.stream().skip(page.getOffset()).limit(page.getPageSize()).collect(Collectors.toList());

        refreshListDto.setHighlightList(superbSubmit);
        refreshListDto.setList(normalSubmit);
        refreshListDto.setEnd(page.isLastPage());
        return WebUtils.result(refreshListDto);
    }

    @RequestMapping("/load/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadApplication(PCLoginUser loginUser,
                                                               @PathVariable Integer applicationId) {
        ApplicationPractice applicationPractice = practiceService.loadApplication(applicationId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("应用练习提交")
                .action("加载应用练习")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);

        return WebUtils.result(applicationPractice);
    }

    @RequestMapping("/update/{applicationId}")
    public ResponseEntity<Map<String, Object>> saveApplicationPractice(PCLoginUser loginUser,
                                                                       @PathVariable Integer applicationId,
                                                                       @RequestBody ApplicationDto applicationDto) {
        Integer result = applicationService.updateApplicationPractice(applicationId, applicationDto.getTopic(), applicationDto.getDescription(),applicationDto.getDifficulty());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("应用练习管理")
                .action("更改应用练习题干")
                .memo(String.valueOf(applicationId));
        operationLogService.log(operationLog);
        if(result == 1) {
            return WebUtils.success();
        } else {
            return WebUtils.error("更新失败");
        }
    }

    @RequestMapping(value = "/insert/practice",method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> insertApplicationPractice(PCLoginUser loginUser, @RequestBody ApplicationPractice applicationPractice){
        Assert.notNull(loginUser,"用户不能为空");

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理")
                .function("应用题新增")
                .action("新增应用题");
        operationLogService.log(operationLog);

        int practiceId = applicationService.insertApplicationPractice(applicationPractice);
        if(practiceId <= 0){
            return WebUtils.error("应用题数据插入失败，请及时联系管理员");
        }
        return WebUtils.success();
    }
}
