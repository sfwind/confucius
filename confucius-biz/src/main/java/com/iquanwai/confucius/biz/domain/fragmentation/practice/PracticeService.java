package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.po.systematism.HomeworkVote;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by justin on 16/12/11.
 */
public interface PracticeService {



    /**
     * 获取挑战训练
     * @param id 挑战训练id
     * @param openid 学员id
     * @param planId 训练计划id
     * */
    ChallengePractice getChallengePractice(Integer id, String openid, Integer planId,boolean create);




    List<ChallengeSubmit> getChallengeSubmitList(Integer challengeId);

    ChallengePractice getChallenge(Integer id);




    ChallengeSubmit loadChallengeSubmit(Integer challengeId);

    /**
     * 查询点赞数
     * @param type 1：挑战任务，2：体系化大作业
     * @param referencedId 被依赖的id
     * @return 点赞数
     */
    Integer loadHomeworkVotesCount(Integer type,Integer referencedId);

    /**
     * 点赞
     * @param type 1：挑战任务，2：体系化大作业
     * @param referencedId 被依赖的id
     * @param openId 点赞的人
     */
    void vote(Integer type, Integer referencedId, String openId,String votedOpenId);

    /**
     * 取消点赞
     */
    Pair<Integer,String> disVote(Integer type, Integer referencedId, String openId);

    /**
     * 查询点赞记录
     */
    HomeworkVote loadVoteRecord(Integer type, Integer referId, String openId);

    /**
     * 查询评论
     */
    List<Comment> loadComments(Integer type,Integer referId,Page page);

    /**
     * 获取评论数
     */
    Integer commentCount(Integer type, Integer referId);

    /**
     * 评论
     */
    Pair<Boolean,String> comment(Integer type, Integer referId, String openId, String content);

    /**
     * 碎片化每日数据
     */
    void fragmentDailyPracticeData();

    /**
     * 增加浏览量
     */
    Integer riseArticleViewCount(Integer module,Integer id, Integer type);

    List<SubjectArticle> loadSubjectArticles(Integer problemId, Page page);

    List<SubjectArticle> loadUserSubjectArticles(Integer problemId, String openId);

    List<ArticleLabel> loadArticleActiveLabels(Integer moduleId, Integer articleId);

    SubjectArticle loadSubjectArticle(Integer submitId);

    Integer submitSubjectArticle(SubjectArticle subjectArticle);

    List<ArticleLabel> updateLabels(Integer module, Integer articleId, List<LabelConfig> labels);

    List<LabelConfig> loadProblemLabels(Integer problemId);

    void updatePicReference(List<String> picList, Integer submitId);

    /**
     * 根据应用id,获取应用训练
     * @param practiceId 练习id
     */
    ApplicationPractice loadApplication(Integer practiceId);

    /**
     * 根据专题id,获取应用训练
     * @param problemId 专题id
     */
    List<ApplicationPractice> loadApplicationByProblemId(Integer problemId);
}
