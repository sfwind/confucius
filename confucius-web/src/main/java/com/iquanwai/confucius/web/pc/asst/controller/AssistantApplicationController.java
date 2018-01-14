package com.iquanwai.confucius.web.pc.asst.controller;

import com.iquanwai.confucius.biz.dao.fragmentation.CommentDao;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.ApplicationService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.Role;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationPractice;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.Comment;
import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;
import com.iquanwai.confucius.biz.po.systematism.HomeworkVote;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.pc.backend.dto.HomeworkVoteDto;
import com.iquanwai.confucius.web.pc.backend.dto.RiseWorkCommentDto;
import com.iquanwai.confucius.web.pc.backend.dto.RiseWorkCommentListDto;
import com.iquanwai.confucius.web.pc.backend.dto.RiseWorkShowDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

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
public class AssistantApplicationController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private ApplicationService applicationService;

    /**
     * 点赞或者取消点赞
     * @param vote 1：点赞，2：取消点赞
     */
    @RequestMapping(value = "/pc/asst/vote", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> vote(PCLoginUser loginUser, @RequestBody HomeworkVoteDto vote) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.isTrue(vote.getStatus() == 1 || vote.getStatus() == 2, "点赞状态异常");
        Integer refer = vote.getReferencedId();
        Integer status = vote.getStatus();
        String openId = loginUser.getOpenId();

        if (status == 1) {
            practiceService.vote(vote.getType(), refer, loginUser.getProfileId(), openId);
        } else {
            // 禁止取消点赞
            logger.error("取消点赞！已禁止!");
            // 取消点赞
//            voteResult = practiceService.disVote(vote.getType(), refer, openId);
//            operationLog.action("取消点赞").memo(loginUser.getOpenId() + "取消点赞" + refer);
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("碎片化")
                .function("小目标")
                .action("点赞")
                .memo(refer.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/pc/asst/comment/{type}/{submitId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadComments(PCLoginUser loginUser,
                                                            @PathVariable("type") Integer type, @PathVariable("submitId") Integer submitId,
                                                            @ModelAttribute Page page) {
        Assert.notNull(type, "评论类型不能为空");
        Assert.notNull(submitId, "文章不能为空");
        Assert.notNull(page, "页码不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("加载评论")
                .memo(type + ":" + submitId);
        operationLogService.log(operationLog);
        List<RiseWorkCommentDto> comments = practiceService.loadComments(type, submitId, page).stream().map(item -> {
            Profile account = accountService.getProfile(item.getCommentProfileId());
            if (account != null) {
                RiseWorkCommentDto dto = new RiseWorkCommentDto();
                dto.setId(item.getId());
                dto.setContent(item.getContent());
                dto.setUpTime(DateUtils.parseDateToFormat5(item.getAddTime()));
                dto.setUpName(account.getNickname());
                dto.setHeadPic(account.getHeadimgurl());
                dto.setRole(account.getRole());
                // dto.setSignature(account.getSignature());
                dto.setIsMine(item.getCommentProfileId().equals(loginUser.getProfileId()));
                if (item.getRepliedId() != null) {
                    Profile replyAccount = accountService.getProfile(item.getRepliedProfileId());
                    dto.setReplyId(item.getRepliedId());
                    dto.setReplyName(replyAccount.getNickname());
                    dto.setReplyContent(item.getRepliedComment());
                }
                return dto;
            } else {
                logger.error("未找到该评论用户:{}", item);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        Integer count = practiceService.commentCount(type, submitId);
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
    @RequestMapping(value = "/pc/asst/comment/{moduleId}/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> comment(PCLoginUser loginUser,
                                                       @PathVariable("moduleId") Integer moduleId, @PathVariable("submitId") Integer submitId,
                                                       @RequestBody RiseWorkCommentDto dto) {
        Assert.notNull(loginUser, "登陆用户不能为空");
        Assert.notNull(moduleId, "评论模块不能为空");
        Assert.notNull(submitId, "文章不能为空");
        Assert.notNull(dto, "内容不能为空");
        Pair<Integer, String> result = practiceService.comment(moduleId, submitId,
                loginUser.getOpenId(), loginUser.getProfileId(), dto.getContent());
        if (result.getLeft() > 0) {
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("训练")
                    .function("碎片化")
                    .action("评论")
                    .memo(moduleId + ":" + submitId);
            operationLogService.log(operationLog);
            RiseWorkCommentDto resultDto = new RiseWorkCommentDto();
            resultDto.setId(result.getLeft());
            resultDto.setContent(dto.getContent());
            resultDto.setUpName(loginUser.getWeixin().getWeixinName());
            resultDto.setHeadPic(loginUser.getWeixin().getHeadimgUrl());
            resultDto.setUpTime(DateUtils.parseDateToFormat5(new Date()));
            resultDto.setRole(loginUser.getRole());
            // resultDto.setSignature(loginUser.getSignature());
            resultDto.setIsMine(true);

            ApplicationSubmit applicationSubmit = practiceService.loadApplicationSubmitById(submitId);

            // 初始化教练回复的评论反馈评价
            if (Role.isAsst(loginUser.getRole()) && !applicationSubmit.getProfileId().equals(loginUser.getProfileId())) {
                practiceService.initCommentEvaluation(submitId, resultDto.getId());
            }

            return WebUtils.result(resultDto);
        } else {
            return WebUtils.error("评论失败");
        }

    }

    /**
     * 评论回复
     * @param loginUser 登录人
     * @param moduleId 评论模块
     * @param submitId 文章id
     * @param dto 评论内容，回复评论id
     */
    @RequestMapping(value = "/pc/asst/comment/reply/{moduleId}/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> commentReply(PCLoginUser loginUser,
                                                            @PathVariable("moduleId") Integer moduleId, @PathVariable("submitId") Integer submitId,
                                                            @RequestBody RiseWorkCommentDto dto) {
        Assert.notNull(loginUser, "登陆用户不能为空");
        Assert.notNull(moduleId, "评论模块不能为空");
        Assert.notNull(submitId, "文章不能为空");
        Assert.notNull(dto, "内容不能为空");
        Pair<Integer, String> result = practiceService.replyComment(moduleId, submitId,
                loginUser.getOpenId(), loginUser.getProfileId(), dto.getContent(), dto.getReplyId());
        if (result.getLeft() > 0) {
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("训练")
                    .function("碎片化")
                    .action("评论")
                    .memo(moduleId + ":" + submitId);
            operationLogService.log(operationLog);
            RiseWorkCommentDto resultDto = new RiseWorkCommentDto();
            resultDto.setId(result.getLeft());
            resultDto.setContent(dto.getContent());
            resultDto.setUpName(loginUser.getWeixin().getWeixinName());
            resultDto.setHeadPic(loginUser.getWeixin().getHeadimgUrl());
            resultDto.setUpTime(DateUtils.parseDateToFormat5(new Date()));
            resultDto.setRole(loginUser.getRole());
            // resultDto.setSignature(loginUser.getSignature());
            resultDto.setIsMine(true);
            if (dto.getReplyId() != null) {
                resultDto.setReplyId(dto.getReplyId());
                Comment replyComment = commentDao.load(Comment.class, dto.getReplyId());
                Profile profile = accountService.getProfile(replyComment.getCommentProfileId());
                if (profile != null) {
                    resultDto.setReplyName(profile.getNickname());
                }
                resultDto.setReplyContent(replyComment.getContent());
            }

            ApplicationSubmit applicationSubmit = practiceService.loadApplicationSubmitById(submitId);

            // 初始化教练回复的评论反馈评价
            if (Role.isAsst(loginUser.getRole()) && !applicationSubmit.getProfileId().equals(loginUser.getProfileId())) {
                practiceService.initCommentEvaluation(submitId, resultDto.getId());
            }

            return WebUtils.result(resultDto);
        } else {
            return WebUtils.error("评论失败");
        }
    }


    @RequestMapping(value = "/pc/asst/request/comment/{moduleId}/{submitId}", method = RequestMethod.POST)
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
            return WebUtils.error("本课程求点评次数已用完");
        }
    }

    @RequestMapping("/pc/asst/delete/comment/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(PCLoginUser loginUser,
                                                             @PathVariable Integer commentId) {

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

    /**
     * 展示应用任务
     *
     * @param loginUser 登陆人
     * @param submitId  提交id
     */
    @RequestMapping("/pc/asst/application/show/{submitId}")
    public ResponseEntity<Map<String, Object>> show(PCLoginUser loginUser, @PathVariable Integer submitId) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("PC查看应用任务内容")
                .memo(loginUser.getOpenId() + " look " + submitId);
        operationLogService.log(operationLog);
        ApplicationSubmit submit = applicationService.loadSubmit(submitId);
        if(submit == null) {
            logger.error("{} has no application submit", loginUser.getOpenId());
            return WebUtils.error(404, "无该提交记录");
        } else {
            // 查到了
            RiseWorkShowDto show = new RiseWorkShowDto();
            show.setSubmitId(submit.getId());
            show.setUpTime(DateUtils.parseDateToFormat5(submit.getPublishTime()));
            show.setContent(submit.getContent());
            show.setType("application");
            show.setRequest(submit.getRequestFeedback());
            // 查询这个openid的数据
            if(loginUser.getProfileId().equals(submit.getProfileId())) {
                // 是自己的
                show.setIsMine(true);
                show.setUpName(loginUser.getWeixin().getWeixinName());
                show.setHeadImg(loginUser.getWeixin().getHeadimgUrl());
                show.setPlanId(submit.getPlanId());
                show.setWorkId(submit.getApplicationId());
                show.setRequestCommentCount(practiceService.hasRequestComment(submit.getPlanId()));
            } else {
                Profile account = accountService.getProfile(loginUser.getProfileId());
                if(account != null) {
                    show.setUpName(account.getNickname());
                    show.setHeadImg(account.getHeadimgurl());
                    show.setSignature(account.getSignature());
                    show.setRole(account.getRole());
                }
                show.setIsMine(false);
            }
            // 查询点赞数
            Integer votesCount = practiceService.loadHomeworkVotesCount(Constants.VoteType.APPLICATION, submit.getId());
            // 查询我对它的点赞状态
            HomeworkVote myVote = practiceService.loadVoteRecord(Constants.VoteType.APPLICATION, submit.getId(), loginUser.getOpenId());
            if(myVote != null && myVote.getDel() == 0) {
                // 点赞中
                show.setVoteStatus(1);
            } else {
                show.setVoteStatus(0);
            }
            show.setVoteCount(votesCount);
            ApplicationPractice applicationPractice = applicationService.loadApplicationPractice(submit.getApplicationId());
            show.setTitle(applicationPractice.getTopic());
            show.setDesc(applicationPractice.getDescription());
            boolean integrated = Knowledge.isReview(applicationPractice.getKnowledgeId());
            if(!integrated) {
                show.setKnowledgeId(applicationPractice.getKnowledgeId());
            }
            // 查询照片
//            List<Picture> pictureList = pictureService.loadPicture(Constants.PictureType.APPLICATION, submit.getId());
//            show.setPicList(pictureList.stream().map(item -> pictureService.getModulePrefix(Constants.PictureType.APPLICATION) + item.getRealName()).collect(Collectors.toList()));
            // 提升浏览量
            practiceService.riseArticleViewCount(Constants.ViewInfo.Module.APPLICATION, submitId, Constants.ViewInfo.EventType.PC_SHOW);
            return WebUtils.result(show);
        }
    }

}
