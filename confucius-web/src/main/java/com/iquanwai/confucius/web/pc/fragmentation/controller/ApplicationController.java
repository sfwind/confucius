package com.iquanwai.confucius.web.pc.fragmentation.controller;

import com.iquanwai.confucius.biz.domain.course.file.PictureService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepoImpl;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.ApplicationService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.HomeworkVote;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.Picture;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationPractice;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.web.pc.dto.ChallengeSubmitDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkEditDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkInfoDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkShowDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.modelmapper.internal.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2017/1/14.
 */
@RestController
@RequestMapping("/pc/fragment/application")
public class ApplicationController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PlanService planService;

    /**
     * 获取应用训练标题
     *
     * @param loginUser     登陆人
     * @param applicationId 应用训练Id
     */
    @RequestMapping("/title/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadApplicationTitle(PCLoginUser loginUser, @PathVariable Integer applicationId) {
        Assert.notNull(applicationId, "应用训练id不能为空");
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
     * 获取应用训练
     *
     * @param loginUser     登陆人
     * @param planId        计划ID
     * @param applicationId 应用训练ID
     * @return
     */
    @RequestMapping("/mine/{planId}/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadMineApplication(PCLoginUser loginUser,
                                                                   @PathVariable("planId") Integer planId,
                                                                   @PathVariable("applicationId") Integer applicationId) {
        Assert.notNull(loginUser,"用户信息不能为空");
        Assert.notNull(planId, "计划id不能为空");
        Assert.notNull(applicationId, "应用训练id不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("加载自己的应用任务")
                .memo(planId + ":" + applicationId);
        operationLogService.log(operationLog);
        String openId = loginUser.getOpenId();
        // 先检查该用户有没有买这个作业
        List<ImprovementPlan> userPlans = planService.loadUserPlans(openId);
        // 看看这个id在不在
        Optional<ImprovementPlan> plan = userPlans.stream().filter(item -> Objects.equals(item.getId(), planId)).findFirst();
        if (plan.isPresent()) {
            ApplicationPractice applicationPractice = applicationService.loadMineApplicationPractice(planId, applicationId, loginUser.getOpenId());
            RiseWorkEditDto dto = new RiseWorkEditDto();
            dto.setSubmitId(applicationPractice.getSubmitId());
            dto.setTitle(applicationPractice.getTopic());
            dto.setContent(applicationPractice.getContent());
            dto.setDescription(applicationPractice.getDescription());
            dto.setModuleId(Constants.PictureType.APPLICATION);
            List<Picture> pictureList = pictureService.loadPicture(Constants.PictureType.APPLICATION, dto.getSubmitId());
            dto.setPicList(pictureList
                    .stream()
                    .map(item -> pictureService.getModulePrefix(Constants.PictureType.APPLICATION) + item.getRealName())
                    .collect(Collectors.toList()));
            return WebUtils.result(dto);
        } else {
            logger.error("用户:{},没有该训练计划:{}，应用训练:{}",openId,plan,applicationId);
            return WebUtils.error(ErrorConstants.NOT_PAY_PROBLEM, "未购买的问题");
        }

    }

    /**
     * 应用列表页加载自己的任务
     *
     * @param loginUser     登陆人
     * @param planId        计划Id
     * @param applicationId 挑战任务ID
     */
    @RequestMapping("/list/mine/{planId}/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadMineApplicationList(PCLoginUser loginUser, @PathVariable("planId") Integer planId, @PathVariable("applicationId") Integer applicationId) {
        Assert.notNull(loginUser, "用户信息不能为空");
        Assert.notNull(planId, "计划不能为空");
        Assert.notNull(applicationId, "应用训练不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("应用任务列表加载自己的应用任务")
                .memo(applicationId + "");
        operationLogService.log(operationLog);
        ApplicationPractice applicationPractice = applicationService.loadMineApplicationPractice(planId, applicationId, loginUser.getOpenId());

        RiseWorkInfoDto info = new RiseWorkInfoDto();
        info.setSubmitId(applicationPractice.getSubmitId());
        info.setTitle(applicationPractice.getTopic());
        info.setContent(applicationPractice.getContent().length() > 180 ?
                applicationPractice.getContent().substring(0, 180) + "......" :
                applicationPractice.getContent());
        info.setHeadPic(loginUser.getWeixin().getHeadimgUrl());
        info.setType(Constants.PracticeType.APPLICATION);
        info.setUpName(loginUser.getWeixin().getWeixinName());
        info.setUpTime(DateUtils.parseDateToFormat5(applicationPractice.getSubmitUpdateTime()));
        info.setVoteCount(practiceService.loadHomeworkVotesCount(Constants.VoteType.APPLICATION, applicationPractice.getSubmitId()));
        return WebUtils.result(info);
    }

    /**
     * 应用任务列表页加载他人的任务信息
     *
     * @param loginUser     登陆人
     * @param applicationId 应用任务Id
     */
    @RequestMapping("/list/other/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadOtherApplicationList(PCLoginUser loginUser, @PathVariable Integer applicationId) {
        Assert.notNull(loginUser, "用户信息不能为空");
        Assert.notNull(applicationId, "应用训练不能为空");
        // 该计划的应用训练是否提交
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("应用任务列表加载他人的应用任务")
                .memo(applicationId + "");
        operationLogService.log(operationLog);
        List<RiseWorkInfoDto> submits = applicationService.loadApplicationSubmitList(applicationId).stream()
                .filter(item -> !item.getOpenid().equals(loginUser.getOpenId())).map(item -> {
                    RiseWorkInfoDto dto = new RiseWorkInfoDto();
                    dto.setContent(item.getContent().length() > 180 ?
                            item.getContent().substring(0, 180) + "......" :
                            item.getContent());
                    dto.setVoteCount(practiceService.loadHomeworkVotesCount(Constants.VoteType.APPLICATION, item.getId()));
                    dto.setUpTime(DateUtils.parseDateToFormat5(item.getUpdateTime()));
                    dto.setType(Constants.PracticeType.APPLICATION);
                    dto.setSubmitId(item.getId());
                    Account account = accountService.getAccount(item.getOpenid(), false);
                    dto.setUpName(account.getNickname());
                    dto.setHeadPic(account.getHeadimgurl());
                    return dto;
                }).collect(Collectors.toList());
        return WebUtils.result(submits);
    }


    /**
     * 提交应用任务
     *
     * @param loginUser          登陆人
     * @param submitId           提交人id
     * @param challengeSubmitDto 任务内容
     */
    @RequestMapping(value = "/submit/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submit(PCLoginUser loginUser,
                                                      @PathVariable Integer submitId,
                                                      @RequestBody ChallengeSubmitDto challengeSubmitDto) {
        Assert.notNull(loginUser, "用户不能为空");
        ApplicationSubmit submit = applicationService.loadSubmit(submitId);
        Boolean result = applicationService.submit(submitId, challengeSubmitDto.getAnswer());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用训练")
                .action("PC提交应用训练答案")
                .memo(submitId + "");
        operationLogService.log(operationLog);
        if (result) {
            if(submit.getPointStatus()==0){
                ApplicationPractice applicationPractice = applicationService.loadApplicationPractice(submit.getApplicationId());
                return WebUtils.result(PointRepoImpl.score.get(applicationPractice.getDifficulty()));
            } else {
                return WebUtils.success();
            }

        } else {
            return WebUtils.error("提交失败");
        }
    }

    /**
     * 展示应用任务
     *
     * @param loginUser 登陆人
     * @param submitId  提交id
     */
    @RequestMapping("/show/{submitId}")
    public ResponseEntity<Map<String, Object>> show(PCLoginUser loginUser, @PathVariable Integer submitId) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("PC查看应用任务内容")
                .memo(loginUser.getOpenId() + " look " + submitId);
        operationLogService.log(operationLog);
        ApplicationSubmit submit = applicationService.loadSubmit(submitId);
        if (submit == null) {
            logger.error("{} has no application submit", loginUser.getOpenId());
            return WebUtils.error(404, "无该提交记录");
        } else {
            // 查到了
            String openId = submit.getOpenid();
            RiseWorkShowDto show = new RiseWorkShowDto();
            show.setSubmitId(submit.getId());
            show.setUpTime(DateUtils.parseDateToFormat5(submit.getUpdateTime()));
            show.setContent(submit.getContent());
            show.setType("application");
            // 查询这个openid的数据
            if (loginUser.getOpenId().equals(openId)) {
                // 是自己的
                show.setIsMine(true);
                show.setUpName(loginUser.getWeixin().getWeixinName());
                show.setHeadImg(loginUser.getWeixin().getHeadimgUrl());
                show.setPlanId(submit.getPlanId());
                show.setWorkId(submit.getApplicationId());
            } else {
                Account account = accountService.getAccount(openId, false);
                if (account != null) {
                    show.setUpName(account.getNickname());
                    show.setHeadImg(account.getHeadimgurl());
                }
                show.setIsMine(false);
            }
            // 查询点赞数
            Integer votesCount = practiceService.loadHomeworkVotesCount(Constants.VoteType.APPLICATION, submit.getId());
            // 查询我对它的点赞状态
            HomeworkVote myVote = practiceService.loadVoteRecord(Constants.VoteType.APPLICATION, submit.getId(), loginUser.getOpenId());
            if (myVote != null && myVote.getDel() == 0) {
                // 点赞中
                show.setVoteStatus(1);
            } else {
                show.setVoteStatus(0);
            }
            show.setVoteCount(votesCount);
            // 根据challengeId查询problemId
            show.setTitle(applicationService.loadApplicationPractice(submit.getApplicationId()).getTopic());
            // 查询照片
            List<Picture> pictureList = pictureService.loadPicture(Constants.PictureType.APPLICATION, submit.getId());
            show.setPicList(pictureList.stream().map(item -> pictureService.getModulePrefix(Constants.PictureType.APPLICATION) + item.getRealName()).collect(Collectors.toList()));
            return WebUtils.result(show);
        }
    }

}
