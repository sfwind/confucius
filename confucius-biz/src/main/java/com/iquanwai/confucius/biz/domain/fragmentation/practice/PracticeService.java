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
     * 查询点赞数
     * @param type 1：小目标，2：体系化大作业
     * @param referencedId 被依赖的id
     * @return 点赞数
     */
    Integer loadHomeworkVotesCount(Integer type, Integer referencedId);

    /**
     * 点赞
     * @param type 1：小目标，2：体系化大作业
     * @param referencedId 被依赖的id
     * @param openId 点赞的人
     */
    boolean vote(Integer type, Integer referencedId, Integer profileId, String openId);

    /**
     * 取消点赞
     */
    Pair<Integer, String> disVote(Integer type, Integer referencedId, String openId);

    /**
     * 查询点赞记录
     */
    HomeworkVote loadVoteRecord(Integer type, Integer referId, String openId);

    /**
     * 查询评论
     */
    List<Comment> loadComments(Integer type, Integer referId, Page page);

    /**
     * 获取评论数
     */
    Integer commentCount(Integer type, Integer referId);

    /**
     * 评论
     */
    Pair<Integer, String> comment(Integer type, Integer referId, String openId, Integer profileId, String content);

    /**
     * 评论(replyId为该评论针对于哪条评论返回)
     */
    Pair<Integer, String> replyComment(Integer type, Integer referId, String openId,
                                       Integer profileId, String content, Integer replyId);

    /**
     * 碎片化每日数据
     */
    void fragmentDailyPracticeData();

    /**
     * 增加浏览量
     */
    Integer riseArticleViewCount(Integer module, Integer id, Integer type);

    List<SubjectArticle> loadSubjectArticles(Integer problemId, Page page);

    List<SubjectArticle> loadUserSubjectArticles(Integer problemId, String openId);

    List<ArticleLabel> loadArticleActiveLabels(Integer moduleId, Integer articleId);

    SubjectArticle loadSubjectArticle(Integer submitId);

    Integer submitSubjectArticle(SubjectArticle subjectArticle);

    List<ArticleLabel> updateLabels(Integer module, Integer articleId, List<LabelConfig> labels);

    List<LabelConfig> loadProblemLabels(Integer problemId);

    void updatePicReference(List<String> picList, Integer submitId);

    /**
     * 根据应用id,获取练习训练
     * @param practiceId 练习id
     */
    ApplicationPractice loadApplication(Integer practiceId);

    /**
     * 根据小课id,获取应用练习
     * @param problemId 小课id
     */
    List<ApplicationPractice> loadApplicationByProblemId(Integer problemId);

    /**
     * 是否可以求点评
     * @param planId 计划id
     */
    Integer hasRequestComment(Integer planId);

    /**
     * 求点评
     * @param submitId 文章提交id
     * @param moduleId 模块id（2-应用练习,3-小课分享）
     */
    boolean requestComment(Integer submitId, Integer moduleId);

    /**
     * 删除评论
     * @param commentId 评论id
     */
    void deleteComment(Integer commentId);

    /**
     * 插入巩固练习数据，返回主键 id
     */
    Integer insertWarmupPractice(WarmupPractice warmupPractice);

    /**
     * 多个选择题分别插入 Choice 表
     */
    void insertWarmupChoice(Integer questionId, List<WarmupChoice> choices);

    /**
     * 根据 WarmupPracticeId 加载其余 WarmupPractice 信息
     */
    WarmupPractice loadWarmupPracticeByPracticeUid(String practiceUid);


    Integer loadWarmupPracticeCntByPracticeUid(String practiceUid);

    /**
     * 根据 PracticeUid 删除 WarmupPractice
     */
    Integer delWarmupPracticeByPracticeUid(String practiceUid);

    void initCommentEvaluation(Integer submitId, Integer commentId);

    ApplicationSubmit loadApplocationSubmitById(Integer applicationSubmitId);
}
