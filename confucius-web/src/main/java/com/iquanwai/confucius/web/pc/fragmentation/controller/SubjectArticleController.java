package com.iquanwai.confucius.web.pc.fragmentation.controller;

import com.iquanwai.confucius.biz.domain.course.file.PictureService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.backend.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.RiseWorkInfoDto;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.ArticleLabel;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.LabelConfig;
import com.iquanwai.confucius.biz.po.fragmentation.SubjectArticle;
import com.iquanwai.confucius.biz.po.systematism.HomeworkVote;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.HtmlRegexpUtil;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RefreshListDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkShowDto;
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
import java.util.stream.Collectors;

/**
 * Created by justin on 17/5/3.
 * 小课分享controller
 */
@RestController
@RequestMapping("/pc/fragment/subject")
public class SubjectArticleController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PlanService planService;
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private ProblemService problemService;

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> loadSubjectList(PCLoginUser loginUser, @RequestParam Integer problemId,
                                                              @ModelAttribute Page page){
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemId, "小课id不能为空");
        page.setPageSize(20);
        List<RiseWorkInfoDto> list = practiceService.loadSubjectArticles(problemId,page)
                .stream().map(item -> {
                    RiseWorkInfoDto dto = new RiseWorkInfoDto(item);
                    Profile account = accountService.getProfile(item.getOpenid(), false);
                    if(account!=null) {
                        dto.setUpName(account.getNickname());
                        dto.setHeadPic(account.getHeadimgurl());
                        dto.setRole(account.getRole());
                        dto.setSignature(account.getSignature());
                    }
                    // 查询我对它的点赞状态
                    if(item.getOpenid().equals(loginUser.getOpenId())){
                        dto.setIsMine(true);
                        ImprovementPlan improvementPlan = planService.loadUserPlan(item.getOpenid(), item.getProblemId());
                        if(improvementPlan!=null){
                            dto.setRequestCommentCount(practiceService.hasRequestComment(improvementPlan.getId()));
                        }
                    }else{
                        dto.setIsMine(false);
                    }

                    if(item.getContent()!=null) {
                        item.setContent(HtmlRegexpUtil.filterHtml(item.getContent()));
                        dto.setContent(
                                item.getContent().length() > 180 ?
                                        item.getContent().substring(0, 180) + "......" :
                                        item.getContent());
                    }
                    return dto;
                }).collect(Collectors.toList());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("PC端加载小课论坛")
                .memo(problemId + "");
        operationLogService.log(operationLog);
        RefreshListDto<RiseWorkInfoDto> result = new RefreshListDto<>();
        result.setList(list);
        result.setEnd(page.isLastPage());
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/list/mine",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> loadMineSubjectList(PCLoginUser loginUser, @RequestParam Integer problemId){
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemId, "小课id不能为空");
        List<RiseWorkInfoDto> list = practiceService.loadUserSubjectArticles(problemId,loginUser.getOpenId())
                .stream().map(item -> {
                    RiseWorkInfoDto dto = new RiseWorkInfoDto(item);
                    item.setContent(HtmlRegexpUtil.filterHtml(item.getContent()));
                    if(item.getContent()!=null) {
                        dto.setContent(
                                item.getContent().length() > 180 ?
                                        item.getContent().substring(0, 180) + "......" :
                                        item.getContent());
                    }
                    dto.setVoteCount(practiceService.loadHomeworkVotesCount(Constants.VoteType.SUBJECT, item.getId()));
                    Profile account = accountService.getProfile(item.getOpenid(), false);
                    if(account!=null) {
                        dto.setUpName(account.getNickname());
                        dto.setHeadPic(account.getHeadimgurl());
                        dto.setRole(account.getRole());
                        dto.setSignature(account.getSignature());
                    }
                    dto.setCommentCount(practiceService.commentCount(Constants.CommentModule.SUBJECT, item.getId()));
                    dto.setIsMine(true);
                    ImprovementPlan improvementPlan = planService.loadUserPlan(item.getOpenid(), item.getProblemId());
                    if(improvementPlan!=null){
                        dto.setRequestCommentCount(practiceService.hasRequestComment(improvementPlan.getId()));
                    }
                    return dto;
                }).collect(Collectors.toList());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("PC端加载小课论坛")
                .memo(problemId + "");
        operationLogService.log(operationLog);
        return WebUtils.result(list);

    }


    @RequestMapping(value = "/{submitId}")
    public ResponseEntity<Map<String, Object>> showSubject(PCLoginUser loginUser, @PathVariable Integer submitId) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(submitId, "提交id不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("PC查看小课论坛内容")
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
            show.setUpTime(DateUtils.parseDateToFormat5(submit.getAddTime()));
            show.setContent(submit.getContent());
            show.setType("subject");
            show.setRequest(submit.getRequestFeedback());
            // 查询这个openid的数据
            if (loginUser.getOpenId().equals(openId)) {
                // 是自己的
                show.setIsMine(true);
                show.setUpName(loginUser.getWeixin().getWeixinName());
                show.setHeadImg(loginUser.getWeixin().getHeadimgUrl());
                show.setWorkId(submit.getProblemId());
                ImprovementPlan improvementPlan = planService.loadUserPlan(openId, submit.getProblemId());
                if(improvementPlan!=null){
                    show.setRequestCommentCount(practiceService.hasRequestComment(improvementPlan.getId()));
                }
            } else {
                Profile account = accountService.getProfile(openId, false);
                if (account != null) {
                    show.setUpName(account.getNickname());
                    show.setHeadImg(account.getHeadimgurl());
                    show.setSignature(account.getSignature());
                    show.setRole(account.getRole());
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
//            List<Picture> pictureList = pictureService.loadPicture(Constants.PictureType.SUBJECT, submit.getId());
//            show.setPicList(pictureList.stream().map(item -> pictureService.getModulePrefix(Constants.PictureType.SUBJECT) + item.getRealName()).collect(Collectors.toList()));
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


    @RequestMapping(value = "/submit/{problemId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitSubjectArticle(PCLoginUser loginUser,
                                                                    @PathVariable("problemId") Integer problemId,
                                                                    @RequestBody RiseWorkInfoDto workInfoDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemId, "难题不能为空");
        boolean b = planService.hasProblemPlan(loginUser.getOpenId(), problemId);
        if(!b){
            return WebUtils.error("您并没有该小课，无法提交");
        }
        Integer submitId = practiceService.submitSubjectArticle(new SubjectArticle(
                workInfoDto.getSubmitId(),
                loginUser.getOpenId(),
                loginUser.getProfileId(),
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

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("PC小课论坛提交")
                .memo(submitId + "");
        operationLogService.log(operationLog);
        if(submitId==-1){
            return WebUtils.error("提交失败,请保存提交内容，并联系管理员");
        }
        practiceService.updateLabels(Constants.LabelArticleModule.SUBJECT, submitId, workInfoDto.getLabelList());
        practiceService.riseArticleViewCount(Constants.ViewInfo.Module.SUBJECT, submitId, Constants.ViewInfo.EventType.PC_SUBMIT);
        return WebUtils.success();
    }

    @RequestMapping(value = "/label/{problemId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadLabels(PCLoginUser loginUser, @PathVariable Integer problemId) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemId, "小课不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("标签")
                .action("加载小课标签")
                .memo(problemId.toString());
        operationLogService.log(operationLog);
        RiseWorkShowDto dto = new RiseWorkShowDto();
        List<LabelConfig> labelConfigs = practiceService.loadProblemLabels(problemId);
        labelConfigs.forEach(item -> item.setSelected(false));
        dto.setLabelList(labelConfigs);
        dto.setDesc(problemService.getProblem(problemId).getSubjectDesc());
        return WebUtils.result(dto);
    }

}
