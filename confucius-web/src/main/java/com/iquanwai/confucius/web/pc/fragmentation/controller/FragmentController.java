package com.iquanwai.confucius.web.pc.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.fragmentation.CommentDao;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepoImpl;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.ApplicationService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.ChallengeService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.pc.fragmentation.dto.*;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2016/12/29.
 */
@RestController
@RequestMapping
public class FragmentController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PlanService planService;
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private ChallengeService challengeService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private PointRepo pointRepo;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CommentDao commentDao;

    /**
     * 点击fragment菜单后，确定需要跳转到的位置
     */
    @RequestMapping(value = "/community", method = RequestMethod.GET)
    public void fragmentGoWhere(HttpServletRequest request, HttpServletResponse response, PCLoginUser loginUser) {
        /**
         * 1.检查用户是否是我们的学员
         * 2.检查正在进行的任务
         */
        try {
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("训练")
                    .function("碎片化")
                    .action("跳转碎片化页面");
            operationLogService.log(operationLog);
            response.sendRedirect("/fragment/rise");
        } catch (Exception e) {
            logger.error("前往碎片化失败");
        }
    }

    /**
     * 碎片化总任务列表加载
     * @param problemId 问题id
     * @param pcLoginUser 登陆人
     */
    @RequestMapping("/pc/fragment/homework/{problemId}")
    public ResponseEntity<Map<String, Object>> getProblemHomeworkList(@PathVariable Integer problemId, PCLoginUser pcLoginUser) {
        Assert.notNull(problemId, "问题id不能为空");
        Assert.notNull(pcLoginUser, "用户信息能不能为空");
        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("总任务列表加载")
                .memo(problemId.toString());
        operationLogService.log(operationLog);
        // 查询该用户有没有购买过这个问题的计划
        ImprovementPlan matchPlan = planService.loadUserPlan(pcLoginUser.getOpenId(), problemId);
        if (matchPlan == null) {
            logger.error("用户:{} 未购买小课:{}", pcLoginUser.getOpenId(), problemId);
            return WebUtils.error(ErrorConstants.NOT_PAY_FRAGMENT, "没找到进行中的RISE训练");
        } else {
            // 购买过小课
            RiseWorkListDto riseHomework = loadUserRiseWork(matchPlan);
            return WebUtils.result(riseHomework);
        }
    }

    /**
     * 点赞或者取消点赞
     *
     * @param vote 1：点赞，2：取消点赞
     */
    @RequestMapping(value = "/pc/fragment/vote", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> vote(PCLoginUser loginUser, @RequestBody HomeworkVoteDto vote) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.isTrue(vote.getStatus() == 1 || vote.getStatus() == 2, "点赞状态异常");
        Integer refer = vote.getReferencedId();
        Integer status = vote.getStatus();
        String openId = loginUser.getOpenId();
        Pair<Integer, String> voteResult;
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("碎片化")
                .function("小目标");
        // TODO 业务逻辑下沉
        if (status == 1) {
            // 点赞加积分
            Integer planId = null;
            String submitOpenId = null;
            if(vote.getType()== Constants.VoteType.CHALLENGE){
                // 小目标点赞
                ChallengeSubmit submit = challengeService.loadSubmit(refer);
                planId = submit.getPlanId();
                submitOpenId = submit.getOpenid();
            } else if(vote.getType() == Constants.VoteType.APPLICATION) {
                // 应用任务点赞
                ApplicationSubmit submit = applicationService.loadSubmit(refer);
                planId = submit.getPlanId();
                submitOpenId = submit.getOpenid();
            } else if(vote.getType() == Constants.VoteType.SUBJECT){
                // 小课论坛点赞
                SubjectArticle submit = practiceService.loadSubjectArticle(refer);
                submitOpenId = submit.getOpenid();
                List<ImprovementPlan> improvementPlans = planService.loadUserPlans(submitOpenId);
                for(ImprovementPlan plan:improvementPlans){
                    if (plan.getProblemId().equals(submit.getProblemId())) {
                        planId = plan.getId();
                    }
                }
            }
            // 点赞
            practiceService.vote(vote.getType(), refer, openId, submitOpenId);
            pointRepo.risePoint(planId,2);
            pointRepo.riseCustomerPoint(submitOpenId,2);
            operationLog.action("点赞").memo(loginUser.getOpenId() + "点赞" + refer);
        } else {
            // 禁止取消点赞
            logger.error("取消点赞！已禁止!");
            // 取消点赞
//            voteResult = practiceService.disVote(vote.getType(), refer, openId);
//            operationLog.action("取消点赞").memo(loginUser.getOpenId() + "取消点赞" + refer);
        }

        operationLogService.log(operationLog);
        return WebUtils.success();
//        if (voteResult.getLeft() == 1) {
//            return WebUtils.success();
//        } else {
//            return WebUtils.error(voteResult.getRight());
//        }
    }

    @RequestMapping(value = "/pc/fragment/comment/{type}/{submitId}",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> loadComments(PCLoginUser loginUser,
                                                           @PathVariable("type") Integer type, @PathVariable("submitId") Integer submitId,
                                                           @ModelAttribute Page page){
        Assert.notNull(type, "评论类型不能为空");
        Assert.notNull(submitId, "文章不能为空");
        Assert.notNull(page, "页码不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("加载评论")
                .memo(type+":"+submitId);
        operationLogService.log(operationLog);
        List<RiseWorkCommentDto> comments = practiceService.loadComments(type, submitId,page).stream().map(item->{
            Profile account = accountService.getProfile(item.getCommentOpenId(), false);
            if(account!=null){
                RiseWorkCommentDto dto = new RiseWorkCommentDto();
                dto.setId(item.getId());
                dto.setContent(item.getContent());
                dto.setUpTime(DateUtils.parseDateToFormat5(item.getAddTime()));
                dto.setUpName(account.getNickname());
                dto.setHeadPic(account.getHeadimgurl());
                dto.setRole(account.getRole());
                // dto.setSignature(account.getSignature());
                dto.setIsMine(item.getCommentOpenId().equals(loginUser.getOpenId()));
                if(item.getRepliedId() != null) {
                    Profile replyAccount = accountService.getProfile(item.getRepliedOpenId(), false);
                    dto.setReplyId(item.getRepliedId());
                    dto.setReplyName(replyAccount.getNickname());
                    dto.setReplyContent(item.getRepliedComment());
                }
                return dto;
            } else {
                logger.error("未找到该评论用户:{}",item);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());;
        Integer count = practiceService.commentCount(type,submitId);
        RiseWorkCommentListDto listDto = new RiseWorkCommentListDto();
        listDto.setCount(count);
        listDto.setList(comments);
        return WebUtils.result(listDto);
    }

    /**
     * 评论
     * TODO 根据角色设置评论类型
     * @param loginUser 登陆人
     * @param moduleId 评论模块
     * @param submitId 文章id
     * @param dto 评论内容
     */
    @RequestMapping(value = "/pc/fragment/comment/{moduleId}/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> comment(PCLoginUser loginUser,
                                                           @PathVariable("moduleId") Integer moduleId, @PathVariable("submitId") Integer submitId,
                                                           @RequestBody RiseWorkCommentDto dto) {
        Assert.notNull(loginUser,"登陆用户不能为空");
        Assert.notNull(moduleId, "评论模块不能为空");
        Assert.notNull(submitId, "文章不能为空");
        Assert.notNull(dto, "内容不能为空");
        Pair<Integer, String> result = practiceService.comment(moduleId, submitId, loginUser.getOpenId(), dto.getContent(), dto.getReplyId());
        if(result.getLeft()>0){
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("训练")
                    .function("碎片化")
                    .action("评论")
                    .memo(moduleId+":"+submitId);
            operationLogService.log(operationLog);
            RiseWorkCommentDto resultDto = new RiseWorkCommentDto();
            resultDto.setId(result.getLeft());
            resultDto.setContent(dto.getContent());
            resultDto.setUpName(loginUser.getWeixin().getWeixinName());
            resultDto.setHeadPic(loginUser.getWeixin().getHeadimgUrl());
            resultDto.setUpTime(DateUtils.parseDateToFormat5(new Date()));
            resultDto.setRole(loginUser.getRole());
//            resultDto.setSignature(loginUser.getSignature());
            resultDto.setIsMine(true);
            resultDto.setReplyId(dto.getReplyId());
            Comment replyComment = commentDao.load(Comment.class, dto.getReplyId());
            resultDto.setReplyName(accountService.getAccount(replyComment.getRepliedOpenId(), false).getNickname());
            resultDto.setReplyContent(replyComment.getContent());
            return WebUtils.result(resultDto);
        } else {
            return WebUtils.error("评论失败");
        }

    }

    private RiseWorkListDto loadUserRiseWork(ImprovementPlan plan) {
        RiseWorkListDto riseWorkListDto = new RiseWorkListDto();
        riseWorkListDto.setApplicationWorkList(Lists.newArrayList());
        riseWorkListDto.setChallengeWorkList(Lists.newArrayList());

        if (plan != null) {
            // 查询该plan的任务列表
            List<PracticePlan> practicePlans = planService.loadWorkPlanList(plan.getId());
            for (PracticePlan item : practicePlans) {
                RiseWorkItemDto dto = new RiseWorkItemDto();
                dto.setPlanId(plan.getId());
                dto.setType(item.getType());
                dto.setUnlocked(item.getUnlocked());
                dto.setWorkId(Integer.parseInt(item.getPracticeId()));
                dto.setStatus(item.getStatus());
                if (item.getType() == Constants.PracticeType.APPLICATION ||
                        item.getType() == Constants.PracticeType.APPLICATION_REVIEW) {
                    ApplicationPractice applicationPractice = applicationService.loadApplicationPractice(Integer.parseInt(item.getPracticeId()));
                    if (applicationPractice == null) {
                        logger.error("查询应用练习失败,训练计划:{}", item);
                    } else {
                        dto.setTitle(applicationPractice.getTopic());
                        dto.setScore(PointRepoImpl.score.get(applicationPractice.getDifficulty()));
                        riseWorkListDto.getApplicationWorkList().add(dto);
                    }
                } else if (item.getType() == Constants.PracticeType.CHALLENGE) {
//                    ChallengePractice challengePractice = challengeService.loadChallengePractice(Integer.parseInt(item.getPracticeId()));
                    dto.setTitle("设定目标、记录进展、总结心得");
                    dto.setScore(ConfigUtils.getChallengeScore());
                    riseWorkListDto.getChallengeWorkList().add(dto);
                }
            };
        }
        return riseWorkListDto;
    }

    @RequestMapping(value = "/pc/fragment/request/comment/{moduleId}/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> requestComment(PCLoginUser loginUser,
                                                              @PathVariable Integer moduleId,
                                                              @PathVariable Integer submitId) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(moduleId, "评论模块不能为空");
        Assert.notNull(submitId, "文章不能为空");

        boolean result = practiceService.requestComment(submitId, moduleId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("写文章")
                .action("求点评")
                .memo(moduleId + ":" + submitId);
        operationLogService.log(operationLog);
        if (result) {
            return WebUtils.success();
        } else {
            return WebUtils.error("本小课求点评次数已用完");
        }
    }

    @RequestMapping("/pc/fragment/delete/comment/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(PCLoginUser loginUser,
                                                             @PathVariable Integer commentId){

        Assert.notNull(loginUser, "用户不能为空");

        practiceService.deleteComment(commentId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("评论")
                .action("删除评论")
                .memo(commentId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }
}
