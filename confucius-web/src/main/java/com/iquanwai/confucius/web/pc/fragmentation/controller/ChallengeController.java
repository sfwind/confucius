package com.iquanwai.confucius.web.pc.fragmentation.controller;

import com.iquanwai.confucius.biz.domain.course.file.PictureService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.ChallengeService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.systematism.HomeworkVote;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengePractice;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengeSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.web.pc.dto.ChallengeSubmitDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkEditDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkInfoDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkShowDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
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
 * Created by nethunder on 2016/12/26.
 */
@RestController
@RequestMapping("/pc/fragment/challenge")
public class ChallengeController {
    @Autowired
    private ProblemService problemService;
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private PlanService planService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ChallengeService challengeService;

    private Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * 加载挑战训练内容
     * @param pcLoginUser 登陆人
     * @param planId 计划id
     * @param cid 挑战任务id
     */
    @RequestMapping("/mine/{planId}/{cid}")
    public ResponseEntity<Map<String, Object>> loadMineChallenge(PCLoginUser pcLoginUser,
                                                                 @PathVariable("planId") Integer planId,
                                                                 @PathVariable("cid") Integer cid) {
        Assert.notNull(pcLoginUser,"用户信息不能为空");
        Assert.notNull(planId, "计划id不能为空");
        Assert.notNull(cid,"挑战训练id不能为空");
        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("训练")
                .function("挑战训练")
                .action("PC加载挑战训练")
                .memo(planId+":"+cid);
        operationLogService.log(operationLog);
        String openId = pcLoginUser.getOpenId();
        // 先检查该用户有没有买这个作业
        List<ImprovementPlan> userPlans = planService.loadUserPlans(openId);
        // 看看这个id在不在
        Optional<ImprovementPlan> plan = userPlans.stream().filter(item -> Objects.equals(item.getId(), planId)).findFirst();
        if (plan.isPresent()) {
            ImprovementPlan improvementPlan = plan.get();
            ChallengePractice challengePractice = practiceService.getChallengePractice(cid, openId, improvementPlan.getId());
            RiseWorkEditDto dto = new RiseWorkEditDto();
//             result.setPic(param.getPic());
            dto.setSubmitId(challengePractice.getSubmitId());
            dto.setContent(challengePractice.getContent());
            dto.setModuleId(Constants.PictureType.CHALLENGE);
            dto.setPicList(pictureService.loadPicture(Constants.PictureType.CHALLENGE, challengePractice.getSubmitId())
                    .stream().map(item -> pictureService.getModulePrefix(Constants.PictureType.CHALLENGE) + item.getRealName())
                    .collect(Collectors.toList()));
            // 先写死
            dto.setDescription("Hi，欢迎来到圈外社区，这里有很多同路人在和你一起进步！<br/>" +
                    "你的什么目标，可以利用当前学习的专题实现呢？请在这里写下你的小目标，并在接下来的时间，记录你应用所学套路来完成目标的方法吧！" );
            // 生成浏览记录

            return WebUtils.result(dto);
        } else {
            logger.error("用户:{},没有该训练计划:{}，挑战训练:{}",openId,plan,cid);
            return WebUtils.error(ErrorConstants.NOT_PAY_PROBLEM, "未购买的问题");
        }
    }

    /**
     * 展示挑战任务提交内容
     * @param pcLoginUser 登陆人
     * @param submitId 提交id
     */
    @RequestMapping("/show/{submitId}")
    public ResponseEntity<Map<String, Object>> showChallenge(PCLoginUser pcLoginUser, @PathVariable("submitId") Integer submitId) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("挑战")
                .function("挑战任务")
                .action("PC查看挑战任务")
                .memo(pcLoginUser.getOpenId() + " look " + submitId);
        operationLogService.log(operationLog);
        ChallengeSubmit submit = practiceService.loadChallengeSubmit(submitId);
        if (submit == null) {
            return WebUtils.error(404, "无该提交记录");
        } else {
            // 查到了
            String openId = submit.getOpenid();
            RiseWorkShowDto show = new RiseWorkShowDto();
            show.setSubmitId(submit.getId());
            show.setUpTime(DateUtils.parseDateToFormat5(submit.getUpdateTime()));
            show.setContent(submit.getContent());
            show.setType("challenge");
            // 查询这个openid的数据
            if (pcLoginUser.getOpenId().equals(openId)) {
                // 是自己的
                show.setIsMine(true);
                show.setUpName(pcLoginUser.getWeixin().getWeixinName());
                show.setHeadImg(pcLoginUser.getWeixin().getHeadimgUrl());
                show.setPlanId(submit.getPlanId());
                // TODO challengeid
                show.setWorkId(submit.getChallengeId());
            } else {
                Account account = accountService.getAccount(openId, false);
                if (account != null) {
                    show.setUpName(account.getNickname());
                    show.setHeadImg(account.getHeadimgurl());
                }
                show.setIsMine(false);
            }
            // 查询点赞数
            Integer votesCount = practiceService.loadHomeworkVotesCount(Constants.VoteType.CHALLENGE, submit.getId());
            // 查询我对它的点赞状态
            HomeworkVote myVote = practiceService.loadVoteRecord(Constants.VoteType.CHALLENGE, submit.getId(), pcLoginUser.getOpenId());
            if (myVote != null && myVote.getDel() == 0) {
                // 点赞中
                show.setVoteStatus(1);
            } else {
                show.setVoteStatus(0);
            }
            show.setVoteCount(votesCount);
            // 根据challengeId查询problemId
            ChallengePractice challengePractice = practiceService.getChallenge(submit.getChallengeId());
            Problem problem = problemService.getProblem(challengePractice.getProblemId());
            show.setTitle(problem.getProblem());
            // 查询照片
            show.setPicList(pictureService.loadPicture(Constants.PictureType.CHALLENGE, submit.getId())
                    .stream().map(item -> pictureService.getModulePrefix(Constants.PictureType.CHALLENGE) + item.getRealName())
                    .collect(Collectors.toList()));
            // 增加浏览量
            practiceService.riseArticleViewCount(Constants.ViewInfo.Module.CHALLENGE, submitId, Constants.ViewInfo.EventType.PC_SHOW);
            return WebUtils.result(show);
        }
    }


    /**
     * 提交挑战任务
     * @param loginUser 登陆人
     * @param submitId 提交id
     * @param challengeSubmitDto 内容
     */
    @RequestMapping(value = "/submit/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submit(PCLoginUser loginUser,
                                                      @PathVariable Integer submitId,
                                                      @RequestBody ChallengeSubmitDto challengeSubmitDto) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("挑战训练")
                .action("PC提交挑战训练答案")
                .memo(submitId + "");
        operationLogService.log(operationLog);
        Pair<Integer,Integer> result = challengeService.submit(submitId, challengeSubmitDto.getAnswer());
        if (result.getLeft() > 0) {
            // 提升提交数
            practiceService.riseArticleViewCount(Constants.ViewInfo.Module.CHALLENGE, submitId, Constants.ViewInfo.EventType.PC_SUBMIT);
            if (result.getLeft() == 2) {
                return WebUtils.result(result.getRight());
            } else {
                return WebUtils.success();
            }
        } else {
            return WebUtils.error("提交失败");
        }
    }


    /**
     * 挑战任务列表展示自己的
     * @param loginUser 登陆人
     * @param planId 计划id
     * @param challengeId 挑战任务id
     */
    @RequestMapping("/list/mine/{planId}/{challengeId}")
    public ResponseEntity<Map<String, Object>> loadMineChallengeList(PCLoginUser loginUser, @PathVariable("planId") Integer planId, @PathVariable("challengeId") Integer challengeId) {
        Assert.notNull(planId, "planId不能为空");
        Assert.notNull(challengeId, "challengeId不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("挑战训练")
                .action("挑战训练列表加载自己的")
                .memo(planId + ":" + challengeId);
        operationLogService.log(operationLog);
        ChallengePractice challengePractice = challengeService.loadMineChallengePractice(planId, challengeId, loginUser.getOpenId());
        // 查询
        RiseWorkInfoDto info = new RiseWorkInfoDto();
        info.setSubmitId(challengePractice.getSubmitId());
        info.setContent(challengePractice.getContent().length() > 180 ?
                challengePractice.getContent().substring(0, 180) + "......" :
                challengePractice.getContent());
        info.setHeadPic(loginUser.getWeixin().getHeadimgUrl());
        info.setType(Constants.PracticeType.CHALLENGE);
        info.setUpName(loginUser.getWeixin().getWeixinName());
        info.setUpTime(DateUtils.parseDateToFormat5(challengePractice.getSubmitUpdateTime()));
        info.setVoteCount(practiceService.loadHomeworkVotesCount(Constants.VoteType.CHALLENGE, challengePractice.getSubmitId()));
        return WebUtils.result(info);
    }


    @RequestMapping("/list/other/{challengeId}")
    public ResponseEntity<Map<String, Object>> showOtherList(PCLoginUser loginUser, @PathVariable("challengeId") Integer challengeId) {
        Assert.notNull(challengeId, "challengeId不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("挑战训练")
                .action("挑战训练列表加载他人的")
                .memo(challengeId + "");
        operationLogService.log(operationLog);
        List<RiseWorkInfoDto> submits = practiceService.getChallengeSubmitList(challengeId)
                .stream()
                .filter(item -> !item.getOpenid().equals(loginUser.getOpenId()))
                .map(item -> {
                    RiseWorkInfoDto dto = new RiseWorkInfoDto();
                    dto.setSubmitId(item.getId());
                    dto.setType(Constants.PracticeType.CHALLENGE);
                    dto.setContent(item.getContent().length() > 180 ?
                            item.getContent().substring(0, 180) + "......" :
                            item.getContent());
                    dto.setVoteCount(practiceService.loadHomeworkVotesCount(Constants.VoteType.CHALLENGE, item.getId()));
                    Account account = accountService.getAccount(item.getOpenid(), false);
                    dto.setUpName(account.getNickname());
                    dto.setHeadPic(account.getHeadimgurl());
                    dto.setUpTime(DateUtils.parseDateToFormat5(item.getUpdateTime()));
                    dto.setCommentCount(practiceService.commentCount(Constants.CommentModule.CHALLENGE,item.getId()));
                    return dto;
                }).sorted((left,right)->{
                    try {
                        int leftWeight = left.getCommentCount() + left.getVoteCount();
                        int rightWeight = right.getCommentCount() + right.getVoteCount();
                        return rightWeight - leftWeight;
                    } catch (Exception e){
                        logger.error("挑战任务文章排序异常",e);
                        return 0;
                    }
                }).collect(Collectors.toList());
        return WebUtils.result(submits);
    }
}
