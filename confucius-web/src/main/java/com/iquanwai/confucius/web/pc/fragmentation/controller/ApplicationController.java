package com.iquanwai.confucius.web.pc.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.OperationLogDao;
import com.iquanwai.confucius.biz.dao.wx.FollowUserDao;
import com.iquanwai.confucius.biz.domain.course.file.PictureModuleType;
import com.iquanwai.confucius.biz.domain.course.file.PictureService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.ApplicationService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.HomeworkVote;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.Picture;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.resolver.PCLoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.account.dto.AccountDto;
import com.iquanwai.confucius.web.course.dto.PictureDto;
import com.iquanwai.confucius.web.pc.dto.ApplicationDto;
import com.iquanwai.confucius.web.pc.dto.ChallengeShowDto;
import com.iquanwai.confucius.web.pc.dto.ChallengeSubmitDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkEditDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkInfoDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkShowDto;
import org.modelmapper.internal.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

    @RequestMapping("/title/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadApplicationTitle(PCLoginUser loginUser, @PathVariable Integer applicationId) {
        try {
            Assert.notNull(applicationId, "应用训练id不能为空");
            ApplicationPractice applicationPractice = applicationService.loadApplicationPractice(applicationId);
            return WebUtils.result(applicationPractice.getTopic());
        } catch (Exception e) {
            logger.error("查询应用训练标题失败", e);
            return WebUtils.error("查询标题失败");
        }
    }

    @RequestMapping("/mine/{planId}/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadMineApplication(PCLoginUser loginUser,
                                                                   @PathVariable("planId") Integer planId,
                                                                   @PathVariable("applicationId") Integer applicationId) {
        try {
            Assert.notNull(applicationId, "应用训练id不能为空");
            ApplicationPractice applicationPractice = applicationService.loadMineApplicationPractice(planId, applicationId, loginUser.getOpenId());
            RiseWorkEditDto dto = new RiseWorkEditDto();
            dto.setSubmitId(applicationPractice.getSubmitId());
            dto.setTitle(applicationPractice.getTopic());
            dto.setContent(applicationPractice.getContent());
            dto.setDescription(applicationPractice.getDescription());
            dto.setModuleId(Constants.PictureType.APPLICATION);
            List<Picture> pictureList = pictureService.loadPicture(PictureModuleType.CHALLENGE, dto.getSubmitId());
            dto.setPicList(pictureList
                    .stream()
                    .map(item -> pictureService.getModulePrefix(PictureModuleType.CHALLENGE) + item.getRealName())
                    .collect(Collectors.toList()));
            return WebUtils.result(dto);
        } catch (Exception e) {
            logger.error("加载引用训练失败", e);
            return WebUtils.error("加载引用训练失败");
        }
    }

    @RequestMapping("/list/mine/{planId}/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadMineApplicationList(PCLoginUser loginUser, @PathVariable("planId") Integer planId, @PathVariable("applicationId") Integer applicationId) {
        try {
            Assert.notNull(loginUser, "用户信息不能为空");
            Assert.notNull(planId, "计划不能为空");
            Assert.notNull(applicationId, "应用训练不能为空");
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
        } catch (Exception e) {
            logger.error("加载应用任务列表失败", e);
            return WebUtils.error("加载应用任务列表失败");
        }
    }

    @RequestMapping("/list/other/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadOtherApplicationList(PCLoginUser loginUser, @PathVariable Integer applicationId) {
        try {
            Assert.notNull(loginUser, "用户信息不能为空");
            Assert.notNull(applicationId, "应用训练不能为空");
            // 该计划的应用训练是否提交
            //
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
        } catch (Exception e) {
            logger.error("加载应用任务列表失败", e);
            return WebUtils.error("加载应用任务列表失败");
        }
    }


    @RequestMapping("/submit/{submitId}")
    public ResponseEntity<Map<String, Object>> submit(PCLoginUser loginUser,
                                                      @PathVariable Integer submitId,
                                                      @RequestBody ChallengeSubmitDto challengeSubmitDto) {
        try {
            Assert.notNull(loginUser, "用户不能为空");
            Boolean result = applicationService.submit(submitId, challengeSubmitDto.getAnswer());
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("训练")
                    .function("应用训练")
                    .action("PC提交应用训练答案")
                    .memo(submitId + "");
            operationLogService.log(operationLog);
            if (result) {
                return WebUtils.success();
            } else {
                return WebUtils.error("提交失败");
            }
        } catch (Exception e) {
            logger.error("提交应用训练失败,{}", e.getLocalizedMessage());
            return WebUtils.error("提交应用训练失败");
        }
    }

    @RequestMapping("/show/{submitId}")
    public ResponseEntity<Map<String, Object>> show(PCLoginUser loginUser, @PathVariable Integer submitId) {
        try {
            org.springframework.util.Assert.notNull(loginUser, "用户不能为空");
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("碎片化")
                    .function("应用任务")
                    .action("PC查看应用任务")
                    .memo(loginUser.getOpenId() + " look " + submitId);
            operationLogService.log(operationLog);
            ApplicationSubmit submit = applicationService.loadSubmit(submitId);
            if (submit == null) {
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
        } catch (Exception e) {
            logger.error("查看挑战任务失败,{}", e.getLocalizedMessage());
            return WebUtils.error(e.getLocalizedMessage());
        }
    }

}
