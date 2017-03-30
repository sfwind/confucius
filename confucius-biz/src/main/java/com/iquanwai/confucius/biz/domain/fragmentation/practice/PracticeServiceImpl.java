package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.common.file.PictureDao;
import com.iquanwai.confucius.biz.dao.fragmentation.*;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.po.systematism.HomeworkVote;
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

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public ChallengePractice getChallengePracticeNoCreate(Integer challengeId, String openId, Integer planId) {
        Assert.notNull(openId, "openId不能为空");
        ChallengePractice challengePractice = challengePracticeDao.load(ChallengePractice.class, challengeId);

        ChallengeSubmit submit = challengeSubmitDao.load(challengeId, planId, openId);
        if (submit == null || submit.getContent() == null) {
            challengePractice.setSubmitted(false);
        } else {
            challengePractice.setSubmitted(true);
        }
        if (submit != null) {
            challengePractice.setContent(submit.getContent());
            challengePractice.setSubmitId(submit.getId());
            challengePractice.setSubmitUpdateTime(submit.getUpdateTime());
        }
        challengePractice.setPlanId(planId);
        return challengePractice;
    }


    @Override
    public ChallengePractice getChallengePractice(Integer id, String openid, Integer planId) {
        Assert.notNull(openid, "openid不能为空");
        ChallengePractice challengePractice = challengePracticeDao.load(ChallengePractice.class, id);

        ChallengeSubmit submit = challengeSubmitDao.load(id, planId, openid);
        if (submit == null || submit.getContent() == null) {
            challengePractice.setSubmitted(false);
        } else {
            challengePractice.setSubmitted(true);
        }
        //生成挑战训练提交记录
        if (submit == null) {
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
        challengePractice.setContent(submit.getContent());
        challengePractice.setSubmitId(submit.getId());
        challengePractice.setSubmitUpdateTime(submit.getUpdateTime());
        challengePractice.setPlanId(planId);
        return challengePractice;
    }


    @Override
    public List<ChallengeSubmit> getChallengeSubmitList(Integer challengeId) {
        return challengeSubmitDao.loadList(challengeId);
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
    public Pair<Boolean, String> comment(Integer moduleId, Integer referId, String openId, String content) {
        if (moduleId == Constants.CommentModule.CHALLENGE) {
            ChallengeSubmit load = challengeSubmitDao.load(ChallengeSubmit.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(false, "没有该文章");
            }
            //自己给自己评论不提醒
            if (load.getOpenid() != null && !load.getOpenid().equals(openId)) {
                String url = "/rise/static/practice/challenge?id=" + load.getChallengeId();
                messageService.sendMessage("评论了我的小目标", load.getOpenid(), openId, url);
            }
        } else if(moduleId == Constants.CommentModule.APPLICATION) {
            ApplicationSubmit load = applicationSubmitDao.load(ApplicationSubmit.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(false, "没有该文章");
            }
            //自己给自己评论不提醒
            if (load.getOpenid() != null && !load.getOpenid().equals(openId)) {
                String url = "/rise/static/practice/application?id=" + load.getApplicationId();
                messageService.sendMessage("评论了我的应用训练", load.getOpenid(), openId, url);
            }
        } else {
            SubjectArticle load = subjectArticleDao.load(SubjectArticle.class,referId);
            if(load == null){
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(false, "没有该文章");
            }
            //自己给自己评论不提醒
            if (load.getOpenid() != null && !load.getOpenid().equals(openId)) {
                Profile profile = accountService.getProfile(openId, false);
                if (profile != null) {
                    String url = "/rise/static/message/subject/reply?submitId=" + referId;
                    messageService.sendMessage("评论了我的专题分享", load.getOpenid(), openId, url);
                }
            }
        }
        Comment comment = new Comment();
        comment.setModuleId(moduleId);
        comment.setReferencedId(referId);
        comment.setType(Constants.CommentType.STUDENT);
        comment.setContent(content);
        comment.setCommentOpenId(openId);
        comment.setDevice(Constants.Device.PC);
        commentDao.insert(comment);
        return new MutablePair<>(true, "评论成功");
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
}
