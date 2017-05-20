package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.common.file.PictureDao;
import com.iquanwai.confucius.biz.dao.fragmentation.*;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.Role;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.po.systematism.HomeworkVote;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/11.
 */
@Service
public class PracticeServiceImpl implements PracticeService {
    @Autowired
    private ChallengePracticeDao challengePracticeDao;
    @Autowired
    private ChallengeSubmitDao challengeSubmitDao;
    @Autowired
    private HomeworkVoteDao homeworkVoteDao;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private FragmentAnalysisDataDao fragmentAnalysisDataDao;
    @Autowired
    private MessageService messageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private SubjectArticleDao subjectArticleDao;
    @Autowired
    private ArticleLabelDao articleLabelDao;
    @Autowired
    private LabelConfigDao labelConfigDao;
    @Autowired
    private PictureDao pictureDao;
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private AsstCoachCommentDao asstCoachCommentDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private RiseMemberDao riseMemberDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ChallengePractice getChallengePractice(Integer id, String openid, Integer planId,boolean create) {
        Assert.notNull(openid, "openid不能为空");
        ChallengePractice challengePractice = challengePracticeDao.load(ChallengePractice.class, id);

        ChallengeSubmit submit = challengeSubmitDao.load(id, planId, openid);
        if (submit == null || submit.getContent() == null) {
            challengePractice.setSubmitted(false);
        } else {
            challengePractice.setSubmitted(true);
        }
        //生成小目标提交记录
        if (submit == null && create) {
            submit = new ChallengeSubmit();
            submit.setOpenid(openid);
            submit.setPlanId(planId);
            submit.setChallengeId(id);
            int submitId = challengeSubmitDao.insert(submit);
            submit.setId(submitId);
            submit.setUpdateTime(new Date());
            // 生成浏览记录
            fragmentAnalysisDataDao.insertArticleViewInfo(ArticleViewInfo.initArticleViews(Constants.ViewInfo.Module.CHALLENGE, submitId));
        }
        challengePractice.setContent(submit==null?null:submit.getContent());
        challengePractice.setSubmitId(submit==null?null:submit.getId());
        challengePractice.setSubmitUpdateTime(submit==null?null:submit.getUpdateTime());
        challengePractice.setPlanId(planId);
        return challengePractice;
    }

    @Override
    public ChallengePractice getChallenge(Integer id) {
        return challengePracticeDao.load(ChallengePractice.class, id);
    }


    @Override
    public ChallengeSubmit loadChallengeSubmit(Integer submitId) {
        return challengeSubmitDao.load(ChallengeSubmit.class, submitId);
    }


    @Override
    public Integer loadHomeworkVotesCount(Integer type, Integer referencedId) {
        return homeworkVoteDao.votedCount(type, referencedId);
    }


    @Override
    public void vote(Integer type, Integer referencedId, String openId, String votedOpenId) {
        HomeworkVote vote = homeworkVoteDao.loadVoteRecord(type, referencedId, openId);
        if (vote == null) {
            homeworkVoteDao.vote(type, referencedId, openId, votedOpenId, Constants.Device.PC);
        } else {
            homeworkVoteDao.reVote(vote.getId());
        }
    }

    @Override
    public Pair<Integer, String> disVote(Integer type, Integer referencedId, String openId) {
        HomeworkVote vote = homeworkVoteDao.loadVoteRecord(type, referencedId, openId);
        if (vote == null) {
            // 没有
            return new MutablePair<>(0, "没有您的点赞记录");
        } else {
            homeworkVoteDao.disVote(vote.getId());
            return new MutablePair<>(1, "success");
        }
    }

    @Override
    public HomeworkVote loadVoteRecord(Integer type, Integer referId, String openId) {
        return homeworkVoteDao.loadVoteRecord(type, referId, openId);
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
    public Pair<Integer, String> comment(Integer moduleId, Integer referId, String openId, String content) {
        boolean isAsst = false;
        Profile profile = accountService.getProfile(openId, false);
        //是否是助教评论
        if(profile!=null){
            isAsst = Role.isAsst(profile.getRole());
        }

        if (moduleId == Constants.CommentModule.CHALLENGE) {
            ChallengeSubmit load = challengeSubmitDao.load(ChallengeSubmit.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(-1, "没有该文章");
            }
            //自己给自己评论不提醒
            if(load.getOpenid()!=null && !load.getOpenid().equals(openId)) {
                String url = "/rise/static/practice/challenge?id=" + load.getChallengeId();
                messageService.sendMessage("评论了我的小目标", load.getOpenid(), openId, url);
            }
        } else if (moduleId == Constants.CommentModule.APPLICATION) {
            ApplicationSubmit load = applicationSubmitDao.load(ApplicationSubmit.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(-1, "没有该文章");
            }
            //更新助教评论状态
            if(isAsst){
                applicationSubmitDao.asstFeedback(load.getId());
                Integer planId = load.getPlanId();
                ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, planId);
                if(plan!=null){
                    asstCoachComment(load.getOpenid(), plan.getProblemId());
                }
            }
            //自己给自己评论不提醒
            if(load.getOpenid()!=null && !load.getOpenid().equals(openId)) {
                String url = "/rise/static/practice/application?id=" + load.getApplicationId();
                messageService.sendMessage("评论了我的应用练习", load.getOpenid(), openId, url);
            }
        } else if(moduleId == Constants.CommentModule.SUBJECT){
            SubjectArticle load = subjectArticleDao.load(SubjectArticle.class,referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(-1, "没有该文章");
            }
            //更新助教评论状态
            if(isAsst){
                subjectArticleDao.asstFeedback(load.getId());
                asstCoachComment(load.getOpenid(), load.getProblemId());
            }
            //自己给自己评论不提醒
            if (load.getOpenid() != null && !load.getOpenid().equals(openId)) {
                String url = "/rise/static/message/subject/reply?submitId=" + referId;
                messageService.sendMessage("评论了我的小课分享", load.getOpenid(), openId, url);
            }
        }
        Comment comment = new Comment();
        comment.setModuleId(moduleId);
        comment.setReferencedId(referId);
        comment.setType(Constants.CommentType.STUDENT);
        comment.setContent(content);
        comment.setCommentOpenId(openId);
        comment.setDevice(Constants.Device.PC);
        int id = commentDao.insert(comment);
        return new MutablePair<>(id,"评论成功");
    }

    @Override
    public Pair<Integer, String> replyComment(Integer moduleId, Integer referId, String openId,
                                              String content, Integer repliedId) {
        Comment repliedComment = commentDao.load(Comment.class, repliedId);
        Comment comment = new Comment();
        comment.setModuleId(moduleId);
        comment.setReferencedId(referId);
        comment.setType(Constants.CommentType.STUDENT);
        comment.setContent(content);
        comment.setCommentOpenId(openId);
        comment.setDevice(Constants.Device.MOBILE);
        if(repliedComment != null) {
            comment.setRepliedOpenId(repliedComment.getCommentOpenId());
            comment.setRepliedId(repliedId);
            comment.setRepliedDel(0);
            comment.setRepliedComment(repliedComment.getContent());
        }
        int id = commentDao.insert(comment);
        //被回复的评论
        if (repliedComment != null && !repliedComment.getCommentOpenId().equals(openId)) {
            String msg = "";
            StringBuilder url = new StringBuilder("/rise/static/message/comment/reply");
            if (moduleId == 2) {
                msg = "评论了我的应用作业";
            } else if (moduleId == 3) {
                msg = "评论了我的小课分享";
            }
            url = url.append("?moduleId=" + moduleId + "&submitId=" + referId + "&commentId=" + id);
            messageService.sendMessage(msg, repliedComment.getCommentOpenId(), openId, url.toString());
        }
        return new MutablePair<>(id, "评论成功");
    }

    private void asstCoachComment(String openId, Integer problemId) {
        AsstCoachComment asstCoachComment =asstCoachCommentDao.loadAsstCoachComment(problemId, openId);
        if(asstCoachComment==null){
            asstCoachComment = new AsstCoachComment();
            asstCoachComment.setCount(1);
            asstCoachComment.setOpenid(openId);
            asstCoachComment.setProblemId(problemId);
            asstCoachCommentDao.insert(asstCoachComment);
        }else{
            asstCoachComment.setCount(asstCoachComment.getCount()+1);
            asstCoachCommentDao.updateCount(asstCoachComment);
        }
    }

    @Override
    public void fragmentDailyPracticeData() {
        logger.info("search fragment daily practice data");
        FragmentDailyData dailyData = fragmentAnalysisDataDao.getDailyData();
        fragmentAnalysisDataDao.insertDailyData(dailyData);
    }

    @Override
    public Integer riseArticleViewCount(Integer module, Integer id, Integer type) {
        return fragmentAnalysisDataDao.riseArticleViewCount(module, id, type);
    }

    @Override
    public List<SubjectArticle> loadSubjectArticles(Integer problemId, Page page) {
        page.setTotal(subjectArticleDao.count(problemId));
        return subjectArticleDao.loadArticles(problemId,page).stream().map(item->{
            item.setVoteCount(homeworkVoteDao.votedCount(Constants.VoteType.SUBJECT, item.getId()));
            item.setCommentCount(commentDao.commentCount(Constants.CommentModule.SUBJECT, item.getId()));
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SubjectArticle> loadUserSubjectArticles(Integer problemId, String openId) {
        return subjectArticleDao.loadArticles(problemId,openId);
    }

    @Override
    public List<ArticleLabel> loadArticleActiveLabels(Integer moduleId, Integer articleId){
        return articleLabelDao.loadArticleActiveLabels(moduleId, articleId);
    }

    @Override
    public SubjectArticle loadSubjectArticle(Integer submitId){
        return subjectArticleDao.load(SubjectArticle.class, submitId);
    }

    @Override
    public Integer submitSubjectArticle(SubjectArticle subjectArticle){
        String content = CommonUtils.removeHTMLTag(subjectArticle.getContent());
        subjectArticle.setLength(content.length());
        Integer submitId = subjectArticle.getId();
        if (subjectArticle.getId()==null){
            // 第一次提交
            submitId = subjectArticleDao.insert(subjectArticle);
            // 生成记录表
            fragmentAnalysisDataDao.insertArticleViewInfo(ArticleViewInfo.initArticleViews(Constants.ViewInfo.Module.SUBJECT, submitId));
        } else {
            // 更新之前的
            subjectArticleDao.update(subjectArticle);
        }
        return submitId;
    }

    @Override
    public List<ArticleLabel> updateLabels(Integer module, Integer articleId, List<LabelConfig> labels){
        List<ArticleLabel> oldLabels = articleLabelDao.loadArticleLabels(module, articleId);
        List<ArticleLabel> shouldDels = Lists.newArrayList();
        List<ArticleLabel> shouldReAdds = Lists.newArrayList();
        labels = labels==null?Lists.newArrayList():labels;
        List<Integer> userChoose = labels.stream().map(LabelConfig::getId).collect(Collectors.toList());
        oldLabels.forEach(item->{
            if(userChoose.contains(item.getLabelId())){
                if(item.getDel()){
                    shouldReAdds.add(item);
                }
            } else {
                shouldDels.add(item);
            }
            userChoose.remove(item.getLabelId());
        });
        userChoose.forEach(item -> articleLabelDao.insertArticleLabel(module, articleId, item));
        shouldDels.forEach(item -> articleLabelDao.updateDelStatus(item.getId(), 1));
        shouldReAdds.forEach(item -> articleLabelDao.updateDelStatus(item.getId(), 0));
        return articleLabelDao.loadArticleActiveLabels(module,articleId);
    }

    @Override
    public List<LabelConfig> loadProblemLabels(Integer problemId){
        return labelConfigDao.loadLabelConfigs(problemId);
    }

    @Override
    public void updatePicReference(List<String> picList, Integer submitId){
        picList.forEach(item->{
           pictureDao.updateReference(item, submitId);
        });
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
        if(improvementPlan==null){
            return null;
        }
        if(improvementPlan.getRequestCommentCount()>0){
            return improvementPlan.getRequestCommentCount();
        }else{
            RiseMember riseMember = riseMemberDao.validRiseMember(improvementPlan.getOpenid());
            if (riseMember == null) {
                // 已经不是会员了就返回null
                return null;
            }
            if(riseMember.getMemberTypeId().equals(RiseMember.ELITE)){
                return 0;
            }
        }
        //非精英用户返回null
        return null;
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
        } else if (moduleId.equals(Constants.Module.SUBJECT)) {
            SubjectArticle subjectArticle = subjectArticleDao.load(SubjectArticle.class, submitId);
            if (subjectArticle.getRequestFeedback()) {
                logger.warn("{} 已经是求点评状态", submitId);
                return true;
            }

            Integer problemId = subjectArticle.getProblemId();
            String openid = subjectArticle.getOpenid();
            ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(openid, problemId);
            if (improvementPlan != null && improvementPlan.getRequestCommentCount() > 0) {
                //更新求点评次数
                improvementPlanDao.updateRequestComment(improvementPlan.getId(), improvementPlan.getRequestCommentCount() - 1);
                //求点评
                subjectArticleDao.requestComment(subjectArticle.getId());
                return true;
            }
        }
        return false;
    }

    public void deleteComment(Integer commentId) {
        commentDao.deleteComment(commentId);
    }
}
