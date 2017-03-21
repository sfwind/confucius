package com.iquanwai.confucius.web.pc.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.course.file.PictureService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepoImpl;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.ApplicationService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.ChallengeService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.Picture;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationPractice;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.ArticleLabel;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengeSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.LabelConfig;
import com.iquanwai.confucius.biz.po.fragmentation.PracticePlan;
import com.iquanwai.confucius.biz.po.fragmentation.SubjectArticle;
import com.iquanwai.confucius.biz.po.systematism.HomeworkVote;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.pc.fragmentation.dto.HomeworkVoteDto;
import com.iquanwai.confucius.web.pc.dto.RefreshListDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkCommentDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkCommentListDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkInfoDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkItemDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkListDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkShowDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private PictureService pictureService;
    @Autowired
    private ProblemService problemService;


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
                    .action("跳转碎片化页面")
                    .memo("");
            operationLogService.log(operationLog);
            response.sendRedirect("/fragment/rise");
        } catch (Exception e) {
            logger.error("前往碎片化失败");
            return;
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
                .memo(problemId+"");
        operationLogService.log(operationLog);
        // 查询该用户有没有购买过这个问题的计划
        List<ImprovementPlan> matchPlans = planService.loadUserPlans(pcLoginUser.getOpenId(),problemId);
        if (matchPlans.isEmpty()) {
            logger.error("用户:{} 未购买专题:{}", pcLoginUser.getOpenId(), problemId);
            return WebUtils.error(ErrorConstants.NOT_PAY_FRAGMENT, "没找到进行中的RISE训练");
        } else {
            // 购买过专题
            RiseWorkListDto riseHomework = loadUserRiseWork(matchPlans, pcLoginUser.getOpenId());
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
                .function("挑战任务");
        // TODO 业务逻辑下沉
        if (status == 1) {
            // 点赞加积分
            Integer planId = null;
            String submitOpenId = null;
            if(vote.getType()== Constants.VoteType.CHALLENGE){
                // 挑战任务点赞
                ChallengeSubmit submit = challengeService.loadSubmit(refer);
                planId = submit.getPlanId();
                submitOpenId = submit.getOpenid();
            } else if(vote.getType() == Constants.VoteType.APPLICATION) {
                // 应用任务点赞
                ApplicationSubmit submit = applicationService.loadSubmit(refer);
                planId = submit.getPlanId();
                submitOpenId = submit.getOpenid();
            } else if(vote.getType() == Constants.VoteType.SUBJECT){
                // 专题区点赞
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
            Account account = accountService.getAccount(item.getCommentOpenId(), false);
            if(account!=null){
                RiseWorkCommentDto dto = new RiseWorkCommentDto();
                dto.setId(item.getId());
                dto.setContent(item.getContent());
                dto.setUpTime(DateUtils.parseDateToFormat5(item.getAddTime()));
                dto.setUpName(account.getNickname());
                dto.setHeadPic(account.getHeadimgurl());
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
        Pair<Boolean, String> result = practiceService.comment(moduleId, submitId, loginUser.getOpenId(), dto.getContent());
        if(result.getLeft()){
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("训练")
                    .function("碎片化")
                    .action("评论")
                    .memo(moduleId+":"+submitId);
            operationLogService.log(operationLog);
            RiseWorkCommentDto resultDto = new RiseWorkCommentDto();
            resultDto.setContent(dto.getContent());
            resultDto.setUpName(loginUser.getWeixin().getWeixinName());
            resultDto.setHeadPic(loginUser.getWeixin().getHeadimgUrl());
            resultDto.setUpTime(DateUtils.parseDateToFormat5(new Date()));
            return WebUtils.result(resultDto);
        } else {
            return WebUtils.error("评论失败");
        }

    }

    @RequestMapping(value = "/pc/fragment/subject/list",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> loadSubjectList(PCLoginUser loginUser, @RequestParam Integer problemId,@ModelAttribute Page page){
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemId, "专题id不能为空");
        page.setPageSize(20);
        List<RiseWorkInfoDto> list = practiceService.loadSubjectArticles(problemId,page)
                .stream().map(item -> {
                    RiseWorkInfoDto dto = new RiseWorkInfoDto(item);
                    Profile account = accountService.getProfile(item.getOpenid(), false);
                    dto.setUpName(account.getNickname());
                    dto.setHeadPic(account.getHeadimgurl());
                    // 查询我对它的点赞状态
                    dto.setIsMine(item.getOpenid().equals(loginUser.getOpenId()));
                    dto.setContent(
                            item.getContent().length() > 180 ?
                                    item.getContent().substring(0, 180) + "......" :
                                    item.getContent());
                    return dto;
                }).collect(Collectors.toList());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("PC端加载专题输出区")
                .memo(problemId + "");
        operationLogService.log(operationLog);
        RefreshListDto<RiseWorkInfoDto> result = new RefreshListDto<>();
        result.setList(list);
        result.setEnd(page.isLastPage());
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/pc/fragment/subject/list/mine",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> loadMineSubjectList(PCLoginUser loginUser, @RequestParam Integer problemId){
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemId, "专题id不能为空");
        List<RiseWorkInfoDto> list = practiceService.loadUserSubjectArticles(problemId,loginUser.getOpenId())
                .stream().map(item -> {
                    RiseWorkInfoDto dto = new RiseWorkInfoDto();
                    dto.setSubmitId(item.getId());
                    dto.setType(Constants.PracticeType.SUBJECT);
                    dto.setContent(
                            item.getContent().length() > 180 ?
                                    item.getContent().substring(0, 180) + "......" :
                                    item.getContent());
                    dto.setVoteCount(practiceService.loadHomeworkVotesCount(Constants.VoteType.SUBJECT, item.getId()));
                    Profile account = accountService.getProfile(item.getOpenid(), false);
                    dto.setUpName(account.getNickname());
                    dto.setHeadPic(account.getHeadimgurl());
                    dto.setUpTime(DateUtils.parseDateToString(item.getUpdateTime()));
                    dto.setCommentCount(practiceService.commentCount(Constants.CommentModule.SUBJECT, item.getId()));
                    dto.setPerfect(item.getSequence() != null && item.getSequence() > 0);
                    dto.setAuthorType(item.getAuthorType());
                    dto.setIsMine(true);
                    dto.setTitle(item.getTitle());
                    return dto;
                }).collect(Collectors.toList());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("PC端加载专题输出区")
                .memo(problemId + "");
        operationLogService.log(operationLog);
        return WebUtils.result(list);

    }


    @RequestMapping(value = "/pc/fragment/subject/{submitId}")
    public ResponseEntity<Map<String, Object>> showSubject(PCLoginUser loginUser, @PathVariable Integer submitId) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(submitId, "提交id不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("PC查看精华分享内容")
                .memo(loginUser.getOpenId() + " look " + submitId);
        operationLogService.log(operationLog);
        SubjectArticle submit = practiceService.loadSubjectArticle(submitId);
        if (submit == null) {
            logger.error("{} has no subject submit", loginUser.getOpenId());
            return WebUtils.error(404, "无该提交记录");
        } else {
            // 查到了
            String openId = submit.getOpenid();
            RiseWorkShowDto show = new RiseWorkShowDto();
            show.setSubmitId(submit.getId());
            show.setUpTime(DateUtils.parseDateToFormat5(submit.getUpdateTime()));
            show.setContent(submit.getContent());
            show.setType("subject");
            // 查询这个openid的数据
            if (loginUser.getOpenId().equals(openId)) {
                // 是自己的
                show.setIsMine(true);
                show.setUpName(loginUser.getWeixin().getWeixinName());
                show.setHeadImg(loginUser.getWeixin().getHeadimgUrl());
                show.setWorkId(submit.getProblemId());
            } else {
                Account account = accountService.getAccount(openId, false);
                if (account != null) {
                    show.setUpName(account.getNickname());
                    show.setHeadImg(account.getHeadimgurl());
                }
                show.setIsMine(false);
            }
            show.setDesc(problemService.getProblem(submit.getProblemId()).getSubjectDesc());
            // 查询点赞数
            Integer votesCount = practiceService.loadHomeworkVotesCount(Constants.VoteType.SUBJECT, submit.getId());
            // 查询我对它的点赞状态
            HomeworkVote myVote = practiceService.loadVoteRecord(Constants.VoteType.SUBJECT, submit.getId(), loginUser.getOpenId());
            if (myVote != null && myVote.getDel() == 0) {
                // 点赞中
                show.setVoteStatus(1);
            } else {
                show.setVoteStatus(0);
            }
            show.setVoteCount(votesCount);
            // 根据challengeId查询problemId
            show.setTitle(submit.getTitle());
            // 查询照片
            List<Picture> pictureList = pictureService.loadPicture(Constants.PictureType.SUBJECT, submit.getId());
            show.setPicList(pictureList.stream().map(item -> pictureService.getModulePrefix(Constants.PictureType.SUBJECT) + item.getRealName()).collect(Collectors.toList()));
            List<LabelConfig> labelConfigs = practiceService.loadProblemLabels(submit.getProblemId());
            List<ArticleLabel> articleLabels = practiceService.loadArticleActiveLabels(Constants.LabelArticleModule.SUBJECT, submitId);
            labelConfigs.forEach(item->{
                boolean flag = false;
                for (ArticleLabel label:articleLabels){
                    if (label.getLabelId().equals(item.getId())) {
                        flag = true;
                        break;
                    }
                }
                item.setSelected(flag);
            });

            show.setLabelList(labelConfigs);
            practiceService.riseArticleViewCount(Constants.ViewInfo.Module.SUBJECT, submitId,Constants.ViewInfo.EventType.PC_SHOW);

            return WebUtils.result(show);
        }
    }


    @RequestMapping(value = "/pc/fragment/subject/submit/{problemId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitSubjectArticle(PCLoginUser loginUser,
                                                                    @PathVariable("problemId") Integer problemId,
                                                                    @RequestBody RiseWorkInfoDto workInfoDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemId, "难题不能为空");
        boolean b = planService.hasProblemPlan(loginUser.getOpenId(), problemId);
        if(!b){
            return WebUtils.error("您并没有该专题，无法提交");
        }
        Integer submitId = practiceService.submitSubjectArticle(new SubjectArticle(
                workInfoDto.getSubmitId(),
                loginUser.getOpenId(),
                problemId,
                1,
                0,
                workInfoDto.getTitle(),
                workInfoDto.getContent()
        ));
        List<String> picList = workInfoDto.getPicList();
        if(picList!=null && picList.size() != 0){
            practiceService.updatePicReference(picList,submitId);
        }

        OperationLog operationLog = OperationLog.create()
                .module("训练")
                .function("碎片化")
                .action("移动专题输出区提交")
                .memo(submitId + "");
        operationLogService.log(operationLog);
        if(submitId==-1){
            return WebUtils.error("提交失败,请保存提交内容，并联系管理员");
        }
        practiceService.updateLabels(Constants.LabelArticleModule.SUBJECT, submitId, workInfoDto.getLabelList());
        practiceService.riseArticleViewCount(Constants.ViewInfo.Module.SUBJECT, submitId, Constants.ViewInfo.EventType.PC_SUBMIT);
        return WebUtils.success();
    }

    @RequestMapping(value = "/pc/fragment/subject/label/{problemId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadLabels(PCLoginUser loginUser, @PathVariable Integer problemId) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemId, "专题不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("标签")
                .action("加载专题标签")
                .memo(problemId.toString());
        operationLogService.log(operationLog);
        RiseWorkShowDto dto = new RiseWorkShowDto();
        List<LabelConfig> labelConfigs = practiceService.loadProblemLabels(problemId);
        labelConfigs.forEach(item -> item.setSelected(false));
        dto.setLabelList(labelConfigs);
        dto.setDesc(problemService.getProblem(problemId).getSubjectDesc());
        return WebUtils.result(dto);
    }


    private RiseWorkListDto loadUserRiseWork(List<ImprovementPlan> plans, String openId) {
        RiseWorkListDto riseWorkListDto = new RiseWorkListDto();
        riseWorkListDto.setApplicationWorkList(Lists.newArrayList());
        riseWorkListDto.setChallengeWorkList(Lists.newArrayList());
        ImprovementPlan plan = null;
        for (ImprovementPlan item:plans){
            if (item.getStatus() == 1) {
                plan = item;
                break;
            }
        }
        if (plan == null  && plans.size() > 0) {
            plan = plans.get(plans.size() - 1);
        }
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
                if (item.getType() == Constants.PracticeType.APPLICATION) {
                    ApplicationPractice applicationPractice = applicationService.loadApplicationPractice(Integer.parseInt(item.getPracticeId()));
                    if (applicationPractice == null) {
                        logger.error("查询应用训练失败,训练计划:{}", item);
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
}
