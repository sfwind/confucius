package com.iquanwai.confucius.web.pc.controller.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.fragmentation.ChallengeSubmitDao;
import com.iquanwai.confucius.biz.domain.course.file.PictureModuleType;
import com.iquanwai.confucius.biz.domain.course.file.PictureService;
import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.*;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.resolver.PCLoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.course.dto.PictureDto;
import com.iquanwai.confucius.web.pc.dto.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
 * Created by nethunder on 2016/12/26.
 */
@RestController
@RequestMapping("/pc/fragment/c")
public class ChallengeController {
    @Autowired
    private CourseProgressService courseProgressService;
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

    private Logger logger = LoggerFactory.getLogger(getClass());


    @RequestMapping("/mine/{planId}/{cid}")
    public ResponseEntity<Map<String, Object>> loadMineChallenge(PCLoginUser pcLoginUser,
                                                                 @PathVariable("planId") Integer planId,
                                                                 @PathVariable("cid") Integer cid) {
        try {
            Assert.notNull(pcLoginUser);
            String openId = pcLoginUser.getOpenId();
            // 先检查该用户有没有买这个作业
            List<ImprovementPlan> userPlans = planService.loadUserPlans(openId);
            // 看看这个id在不在
            Optional<ImprovementPlan> plan = userPlans.stream().filter(item -> Objects.equals(item.getId(), planId)).findFirst();
            if (plan.isPresent()) {
                // planId正确
                ImprovementPlan improvementPlan = plan.get();
                // 获取该问题的挑战任务
                ImprovementPlan running = planService.getRunningPlan(openId);
                ChallengePractice challengePractice = null;
                if (running.getId() == improvementPlan.getId()) {
                    // 该问题是正在解决的问题
                    challengePractice = practiceService.getChallengePractice(cid, openId, running.getId());
                } else {
                    // 不是正在解决的问题，不能自动生成
                    challengePractice = practiceService.getChallengePracticeNoCreate(cid, openId, improvementPlan.getId());
                    if (!challengePractice.getSubmitted()) {
                        // 不是正在进行的问题，也没做
                        return WebUtils.error(100002, "同学，该挑战任务已超过提交时限");
                    }
                }
                // 转换为dto
                ChallengeDto result = ChallengeDto.getFromPo(challengePractice);
                result.setModuleId(PictureModuleType.CHALLENGE);
                // 查询图片
                // 加载大作业的图片
                List<Picture> pictureList = pictureService.loadPicture(PictureModuleType.CHALLENGE, result.getSubmitId());
                result.setPicList(pictureList.stream().map(item -> {
                    String picUrl = pictureService.getModulePrefix(PictureModuleType.CHALLENGE) + item.getRealName();
                    return new PictureDto(PictureModuleType.CHALLENGE, result.getId(), picUrl);
                }).collect(Collectors.toList()));
                String description = "Hi，欢迎来到圈外社区。<br/>请按照手机端挑战任务的页面提示，在这里记录下你的学习的小目标、感悟或经历吧！";
                result.setDescription(description);
                OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                        .module("训练")
                        .function("挑战训练")
                        .action("PC加载挑战训练")
                        .memo(cid.toString());
                operationLogService.log(operationLog);
                return WebUtils.result(result);

            } else {
                // 没有买这个问题
                return WebUtils.error(100001, "未购买的问题");
            }
        } catch (Exception e) {
            logger.error("加载挑战任务失败,{}", e.getLocalizedMessage());
            return WebUtils.error("加载挑战任务失败");
        }
    }

    @RequestMapping("/show/{submitId}")
    public ResponseEntity<Map<String, Object>> showChallenge(PCLoginUser pcLoginUser, @PathVariable("submitId") Integer submitId) {
        try {
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
                ChallengeShowDto show = new ChallengeShowDto();
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
                    show.setChallengeId(submit.getChallengeId());
                } else {
                    Account account = accountService.getAccount(openId, false);
                    if (account != null) {
                        show.setUpName(account.getNickname());
                        show.setHeadImg(account.getHeadimgurl());
                    }
                    show.setIsMine(false);
                }
                // 查询点赞数
                Integer votesCount = practiceService.loadHomeworkVotesCount(1, submit.getId());
                // 查询我对它的点赞状态
                HomeworkVote myVote = practiceService.loadVoteRecord(1, submit.getId(), pcLoginUser.getOpenId());
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
                show.setProblemId(problem.getId());
                show.setTitle(problem.getProblem());
                // 查询照片
                List<Picture> pictureList = pictureService.loadPicture(PictureModuleType.CHALLENGE, submit.getId());
                show.setPicList(pictureList.stream().map(item -> {
                    String picUrl = pictureService.getModulePrefix(PictureModuleType.CHALLENGE) + item.getRealName();
                    return new PictureDto(PictureModuleType.CHALLENGE, submit.getId(), picUrl);
                }).collect(Collectors.toList()));
                return WebUtils.result(show);
            }
        } catch (Exception e) {
            logger.error("查看挑战任务失败,{}", e.getLocalizedMessage());
            return WebUtils.error(e.getLocalizedMessage());
        }
    }


    @RequestMapping("/submit/{submitId}")
    public ResponseEntity<Map<String, Object>> submit(PCLoginUser loginUser,
                                                      @PathVariable Integer submitId,
                                                      @RequestBody ChallengeSubmitDto challengeSubmitDto) {
        try {
            Assert.notNull(loginUser, "用户不能为空");
            Boolean result = practiceService.submit(submitId, challengeSubmitDto.getAnswer());
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("训练")
                    .function("挑战训练")
                    .action("PC提交挑战训练答案")
                    .memo(submitId + "");
            operationLogService.log(operationLog);
            if (result) {
                return WebUtils.success();
            } else {
                return WebUtils.error("提交失败");
            }
        } catch (Exception e) {
            logger.error("提交挑战训练失败,{}", e.getLocalizedMessage());
            return WebUtils.error("提交挑战训练失败");
        }
    }


    /**
     * 点赞或者取消点赞
     *
     * @param vote 1：点赞，2：取消点赞
     */
    @RequestMapping(value = "vote", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> vote(PCLoginUser loginUser, @RequestBody HomeworkVoteDto vote) {
        try {
            Assert.notNull(loginUser, "用户不能为空");
            Assert.isTrue(vote.getStatus() == 1 || vote.getStatus() == 2, "点赞状态异常");
            Integer refer = vote.getReferencedId();
            Integer status = vote.getStatus();
            String openId = loginUser.getOpenId();
            Pair<Integer, String> voteResult = null;
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("课程")
                    .function("挑战任务");
            if (status == 1) {
                // 点赞
                practiceService.vote(1, refer, openId);
                voteResult = new MutablePair<Integer, String>(1, "success");
                operationLog.action("点赞").memo(loginUser.getOpenId() + "点赞" + refer);
            } else {
                // 取消点赞
                voteResult = practiceService.disVote(1, refer, openId);
                operationLog.action("取消点赞").memo(loginUser.getOpenId() + "取消点赞" + refer);
            }

            operationLogService.log(operationLog);
            if (voteResult.getLeft() == 1) {
                return WebUtils.success();
            } else {
                return WebUtils.error(voteResult.getRight());
            }
        } catch (Exception e) {
            logger.error("点赞失败,{}", e.getLocalizedMessage());
            return WebUtils.error("点赞失败");
        }
    }

    @RequestMapping("/list/mine/{challengeId}")
    ResponseEntity<Map<String, Object>> showList(PCLoginUser loginUser, @PathVariable("challengeId") Integer challengeId) {
        try {
            Assert.notNull(challengeId, "challengeId不能为空");
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("训练")
                    .function("挑战训练")
                    .action("PC加载自己的挑战训练")
                    .memo(challengeId + "");
            operationLogService.log(operationLog);
            // 先查询用户自己的该挑战，是否已提交
            String openId = loginUser.getOpenId();
            List<ImprovementPlan> runningPlans = planService.loadUserPlans(openId);
            List<ImprovementPlan> thisPlans = runningPlans.stream().filter(item -> {
                List<ChallengePractice> challenges = practiceService.getChallengePracticesByProblem(item.getProblemId());
                for (ChallengePractice challenge : challenges) {
                    if (challenge.getId() == challengeId) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());
            // thisPlans是匹配到的计划
            if (thisPlans.isEmpty()) {
                // 该用户没有制定过包含这个挑战任务的计划
                logger.info("{} 用户未制定包含{}挑战任务的问题", openId, challengeId);
                return WebUtils.error("您没有制定过这个问题的解决计划哦");
            } else {
                // 查到了
                List<ChallengeDto> mineChallenges = thisPlans.stream().map(item -> {
                    ChallengeDto dto = null;
                    ChallengePractice challengePractice = null;
                    if (item.getStatus() == 1) {
                        // 是否是正在做的
                        challengePractice = practiceService.getChallengePractice(challengeId, openId, item.getId());
                    } else {
                        // 不是正在做的
                        challengePractice = practiceService.getChallengePracticeNoCreate(challengeId, openId, item.getId());
                    }
                    dto = ChallengeDto.getFromPo(challengePractice);
                    dto.setHeadImg(loginUser.getWeixin().getHeadimgUrl());
                    dto.setUpName(loginUser.getWeixin().getWeixinName());
                    if (dto.getSubmitted()) {
                        // 提交过
                        dto.setContent(dto.getContent().length() > 180 ? dto.getContent().substring(0, 180) + "......" : dto.getContent());
                        dto.setUpTime(DateUtils.parseDateToFormat5(challengePractice.getSubmitUpdateTime()));
                    }
                    dto.setVoteCount(practiceService.loadHomeworkVotesCount(1, challengePractice.getSubmitId()));
                    HomeworkVote vote = practiceService.loadVoteRecord(1, challengeId, openId);
                    dto.setCanVote(vote == null || vote.getDel() == 1);
                    dto.setPlanId(item.getId());
                    dto.setProblemId(challengePractice.getProblemId());
                    return dto;
                }).collect(Collectors.toList());
                return WebUtils.result(mineChallenges);
            }
        } catch (Exception e) {
            logger.error("加载个人信息失败:{}", e.getLocalizedMessage());
            return WebUtils.error("加载个人信息失败");
        }
    }

    @RequestMapping("/list/other/{challengeId}")
    public ResponseEntity<Map<String, Object>> showOtherList(PCLoginUser loginUser, @PathVariable("challengeId") Integer challengeId) {
        try {
            Assert.notNull(challengeId, "challengeId不能为空");
            Assert.notNull(loginUser, "登陆人不能未空");
            //
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("训练")
                    .function("挑战训练")
                    .action("PC加载他人挑战训练")
                    .memo(challengeId + "");
            operationLogService.log(operationLog);
            List<ChallengeSubmit> submits = practiceService.getChallengeSubmitList(challengeId)
                    .stream().filter(item -> !item.getOpenid().equals(loginUser.getOpenId())).collect(Collectors.toList());
            // 过滤掉自己
            if (submits.isEmpty()) {
                return WebUtils.result(submits);
            } else {
                List<ChallengeDto> list = submits.stream().map(item -> {
                    // 这些都是提交的
                    ChallengeDto dto = new ChallengeDto();
                    dto.setContent(item.getContent().length() > 180 ? item.getContent().substring(0, 180) + "......" : item.getContent());
                    dto.setSubmitId(item.getId());
                    dto.setUpTime(DateUtils.parseDateToFormat5(item.getUpdateTime()));
                    // 查询用户信息
                    Account account = accountService.getAccount(item.getOpenid(), false);
                    dto.setUpName(account.getNickname());
                    dto.setHeadImg(account.getHeadimgurl());
                    // 查询点赞信息
                    dto.setVoteCount(practiceService.loadHomeworkVotesCount(1, item.getId()));
                    HomeworkVote vote = practiceService.loadVoteRecord(1, item.getId(), loginUser.getOpenId());
                    dto.setCanVote(vote == null || vote.getDel() == 1);
                    return dto;
                }).collect(Collectors.toList());
                return WebUtils.result(list);
            }
        } catch (Exception e) {
            logger.error("记载他人信息失败", e);
            return WebUtils.error("加载失败");
        }
    }


    private List<ProblemDto> loadAllProblems() {
        List<Problem> problems = problemService.loadProblems();
        List<ProblemDto> problemDtos = Lists.newArrayList();
        problemDtos.addAll(problems.stream().map(problem -> {
            ProblemDto problemDto = new ProblemDto();
            problemDto.setId(problem.getId());
            problemDto.setPic(problem.getPic());
            problemDto.setProblem(problem.getProblem());
            // 获取所有挑战训练
            List<ChallengePractice> challengePractices = practiceService.loadPractice(problem.getId());
            problemDto.setChallengeLis(challengePractices.stream().map(challenge -> {
                ChallengeDto challengeDto = new ChallengeDto();
                challengeDto.setPic(challenge.getPic());
                // 用户的挑战训练置空
                challengeDto.setContent(null);
                challengeDto.setDescription(null);
                challengeDto.setId(challenge.getId());
                challengeDto.setPcurl(challenge.getPcurl());
                challengeDto.setProblemId(challenge.getProblemId());
                return challengeDto;
            }).collect(Collectors.toList()));
            return problemDto;
        }).collect(Collectors.toList()));
        return problemDtos;
    }
}
