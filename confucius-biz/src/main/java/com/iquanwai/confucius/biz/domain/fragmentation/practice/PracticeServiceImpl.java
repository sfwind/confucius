package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.dao.fragmentation.*;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.Role;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.po.systematism.HomeworkVote;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/11.
 */
@Service
public class PracticeServiceImpl implements PracticeService {
    @Autowired
    private HomeworkVoteDao homeworkVoteDao;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private MessageService messageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private AsstCoachCommentDao asstCoachCommentDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private PointRepo pointRepo;
    @Autowired
    private WarmupPracticeDao warmupPracticeDao;
    @Autowired
    private WarmupChoiceDao warmupChoiceDao;
    @Autowired
    private CommentEvaluationDao commentEvaluationDao;
    @Autowired
    private WarmupPracticeDiscussDao warmupPracticeDiscussDao;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Integer loadHomeworkVotesCount(Integer type, Integer referencedId) {
        return homeworkVoteDao.votedCount(type, referencedId);
    }

    @Override
    public boolean vote(Integer type, Integer referencedId, Integer profileId) {
        HomeworkVote vote = homeworkVoteDao.loadVoteRecord(type, referencedId, profileId);
        if (vote == null) {
            Integer planId = null;
            Integer submitProfileId = null;
            if (type == Constants.VoteType.APPLICATION) {
                // 应用任务点赞
                ApplicationSubmit submit = applicationSubmitDao.load(ApplicationSubmit.class, referencedId);
                if (submit == null) {
                    return false;
                }
                planId = submit.getPlanId();
                submitProfileId = submit.getProfileId();
            }
            HomeworkVote homeworkVote = new HomeworkVote();
            homeworkVote.setReferencedId(referencedId);
            homeworkVote.setVoteProfileId(profileId);
            homeworkVote.setType(type);
            homeworkVote.setVotedProfileId(submitProfileId);
            homeworkVote.setDevice(Constants.Device.PC);
            homeworkVoteDao.vote(homeworkVote);
            pointRepo.risePoint(planId, ConfigUtils.getVoteScore());
            pointRepo.riseCustomerPoint(submitProfileId, ConfigUtils.getVoteScore());
        } else {
            homeworkVoteDao.reVote(vote.getId());
        }
        return true;
    }

    @Override
    public HomeworkVote loadVoteRecord(Integer type, Integer referId, Integer profileId) {
        return homeworkVoteDao.loadVoteRecord(type, referId, profileId);
    }

    @Override
    public List<Comment> loadComments(Integer type, Integer referId, Page page) {
        return commentDao.loadComments(type, referId, page);
    }

    @Override
    public Integer commentCount(Integer moduleId, Integer referId) {
        return commentDao.commentCount(moduleId, referId);
    }

    @Override
    public Pair<Integer, String> comment(Integer moduleId, Integer referId,
                                         Integer profileId, String content) {
        //先插入评论
        Comment comment = new Comment();
        comment.setModuleId(moduleId);
        comment.setReferencedId(referId);
        comment.setType(Constants.CommentType.STUDENT);
        comment.setContent(content);
        comment.setCommentProfileId(profileId);
        comment.setDevice(Constants.Device.PC);
        int id = commentDao.insert(comment);

        boolean isAsst = false;
        Profile profile = accountService.getProfile(profileId);
        //是否是助教评论
        if (profile != null) {
            isAsst = Role.isAsst(profile.getRole());
        }
        if (moduleId == Constants.CommentModule.APPLICATION) {
            ApplicationSubmit load = applicationSubmitDao.load(ApplicationSubmit.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(-1, "没有该文章");
            }
            //更新助教评论状态
            if (isAsst) {
                if (load.getFeedBackTime() == null) {
                    applicationSubmitDao.asstFeedbackAndTime(load.getId());
                } else {
                    applicationSubmitDao.asstFeedback(load.getId());
                }
                asstCoachComment(load.getProfileId(), load.getProblemId());
            }
            //自己给自己评论不提醒
            if (load.getProfileId() != null && !load.getProfileId().equals(profileId)) {
                String url = "/rise/static/message/application/reply?submitId=" + referId + "&commentId=" + id;
                messageService.sendMessage("评论了我的应用题", load.getProfileId().toString(), profileId.toString(), url);
            }
        }
        return new MutablePair<>(id, "评论成功");
    }

    @Override
    public Pair<Integer, String> replyComment(Integer moduleId, Integer referId,
                                              Integer profileId, String content, Integer repliedId) {
        // 查看该评论是否为助教回复
        boolean isAsst = false;
        Profile profile = accountService.getProfile(profileId);
        if (profile != null) {
            isAsst = Role.isAsst(profile.getRole());
        }

        if (moduleId == Constants.CommentModule.APPLICATION) {
            ApplicationSubmit load = applicationSubmitDao.load(ApplicationSubmit.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(-1, "没有该文章");
            }
            // 是助教评论
            if (isAsst) {
                // 将此条评论所对应的 ApplicationSubmit 置为已被助教评论
                applicationSubmitDao.asstFeedback(load.getId());
                asstCoachComment(load.getProfileId(), load.getProblemId());
            }
        }

        //被回复的评论
        Comment repliedComment = commentDao.load(Comment.class, repliedId);
        if (repliedComment == null) {
            return new MutablePair<>(-1, "评论失败");
        }

        Comment comment = new Comment();
        comment.setModuleId(moduleId);
        comment.setReferencedId(referId);
        comment.setType(Constants.CommentType.STUDENT);
        comment.setContent(content);
        comment.setCommentProfileId(profileId);
        comment.setRepliedProfileId(repliedComment.getCommentProfileId());
        comment.setRepliedComment(repliedComment.getContent());
        comment.setRepliedDel(0);
        comment.setRepliedId(repliedId);
        comment.setDevice(Constants.Device.PC);

        int id = commentDao.insert(comment);
        //评论自己的评论,不发通知
        if (!repliedComment.getCommentProfileId().equals(profileId)) {
            String msg = "";
            StringBuilder url = new StringBuilder("/rise/static/message/comment/reply");
            if (moduleId == 2) {
                msg = "评论了我的应用题";
            } else if (moduleId == 3) {
                msg = "评论了我的小课分享";
            }
            url = url.append("?moduleId=").append(moduleId).append("&submitId=").append(referId).append("&commentId=").append(id);
            messageService.sendMessage(msg, repliedComment.getCommentProfileId().toString(), profileId.toString(), url.toString());
        }
        return new MutablePair<>(id, "评论成功");
    }

    private void asstCoachComment(Integer profileId, Integer problemId) {
        AsstCoachComment asstCoachComment = asstCoachCommentDao.loadAsstCoachComment(problemId, profileId);
        if (asstCoachComment == null) {
            asstCoachComment = new AsstCoachComment();
            asstCoachComment.setCount(1);
            asstCoachComment.setProfileId(profileId);
            asstCoachComment.setProblemId(problemId);
            asstCoachCommentDao.insert(asstCoachComment);
        } else {
            asstCoachComment.setCount(asstCoachComment.getCount() + 1);
            asstCoachCommentDao.updateCount(asstCoachComment);
        }
    }

    @Override
    public ApplicationPractice loadApplication(Integer practiceId) {
        return applicationPracticeDao.load(ApplicationPractice.class, practiceId);
    }

    @Override
    public List<ApplicationPractice> loadApplicationByProblemId(Integer problemId) {
        return applicationPracticeDao.getPracticeByProblemId(problemId);
    }

    @Override
    public Integer hasRequestComment(Integer planId) {
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
        if (improvementPlan == null) {
            return null;
        }
        return improvementPlan.getRequestCommentCount();
    }

    @Override
    public boolean requestComment(Integer submitId, Integer moduleId) {
        if (moduleId.equals(Constants.Module.APPLICATION)) {
            ApplicationSubmit applicationSubmit = applicationSubmitDao.load(ApplicationSubmit.class, submitId);
            if (applicationSubmit.getRequestFeedback()) {
                logger.warn("{} 已经是求点评状态", submitId);
                return true;
            }
            Integer planId = applicationSubmit.getPlanId();
            ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
            if (improvementPlan != null && improvementPlan.getRequestCommentCount() > 0) {
                //更新求点评次数
                improvementPlanDao.updateRequestComment(planId, improvementPlan.getRequestCommentCount() - 1);
                //求点评
                applicationSubmitDao.requestComment(applicationSubmit.getId());
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteComment(Integer commentId) {
        commentDao.deleteComment(commentId);
    }

    @Override
    public Integer insertWarmupPractice(WarmupPractice warmupPractice) {
        return warmupPracticeDao.insertWarmupPractice(warmupPractice);
    }

    @Override
    public void insertWarmupChoice(Integer questionId, List<WarmupChoice> choices) {
        choices.forEach(choice -> choice.setQuestionId(questionId));
        warmupChoiceDao.batchInsert(choices);
    }

    @Override
    public WarmupPractice loadWarmupPracticeByPracticeUid(String practiceUid) {
        return warmupPracticeDao.loadWarmupPracticeByPracticeUid(practiceUid);
    }

    @Override
    public Integer loadWarmupPracticeCntByPracticeUid(String practiceUid) {
        return warmupPracticeDao.loadWarmupPracticeCntByPracticeUid(practiceUid);
    }

    @Override
    public void initCommentEvaluation(Integer submitId, Integer commentId) {
        Comment comment = commentDao.load(Comment.class, commentId);
        if (comment != null && comment.getCommentProfileId() != null) {
            // 对于一道应用题，只有一次评价
            List<Comment> comments = commentDao.loadCommentsByProfileId(submitId, comment.getCommentProfileId());
            if (comments.size() == 1) {
                commentEvaluationDao.initCommentEvaluation(submitId, commentId);
            }
        }
    }

    @Override
    public ApplicationSubmit loadApplicationSubmitById(Integer applicationSubmitId) {
        return applicationSubmitDao.load(ApplicationSubmit.class, applicationSubmitId);
    }

    @Override
    public Integer deleteExamples(Integer id) {
        return warmupPracticeDao.delWarmupPractice(id);
    }

    @Override
    public List<WarmupPractice> loadWarmupPractices(List<Integer> practiceIds) {
        return warmupPracticeDao.loadPractices(practiceIds);
    }

    @Override
    public List<WarmupPracticeDiscuss> loadYesterdayCommentsByPractice(WarmupPractice warmupPractice) {
        String currentDate = DateUtils.parseDateToString(DateUtils.beforeDays(new Date(), 1));
        return warmupPracticeDiscussDao.loadCurrentDayDiscussByWarmUp(currentDate, warmupPractice);
    }

    @Override
    public List<Integer> loadProblemsByYesterdayComments() {
        String currentDate = DateUtils.parseDateToString(DateUtils.beforeDays(new Date(), 1));
        List<WarmupPracticeDiscuss> warmupPracticeDiscusses = warmupPracticeDiscussDao.loadCurrentDayDiscuss(currentDate);
        List<Integer> warmupPractices = warmupPracticeDiscusses.stream().map(WarmupPracticeDiscuss::getWarmupPracticeId).distinct().collect(Collectors.toList());

        List<WarmupPractice> practices = warmupPracticeDao.loadPractices(warmupPractices);
        return practices.stream().map(WarmupPractice::getProblemId).distinct().collect(Collectors.toList());
    }


}
