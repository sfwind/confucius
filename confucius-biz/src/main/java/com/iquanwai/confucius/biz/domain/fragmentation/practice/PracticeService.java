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
     * @param profileId 点赞的人
     */
    boolean vote(Integer type, Integer referencedId, Integer profileId);

    /**
     * 查询点赞记录
     */
    HomeworkVote loadVoteRecord(Integer type, Integer referId, Integer profileId);

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
    Pair<Integer, String> comment(Integer type, Integer referId, Integer profileId, String content);

    /**
     * 评论(replyId为该评论针对于哪条评论返回)
     */
    Pair<Integer, String> replyComment(Integer type, Integer referId,
                                       Integer profileId, String content, Integer replyId);

    /**
     * 根据应用id,获取练习训练
     * @param practiceId 练习id
     */
    ApplicationPractice loadApplication(Integer practiceId);

    /**
     * 根据课程id,获取应用练习
     * @param problemId 课程id
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


    void initCommentEvaluation(Integer submitId, Integer commentId);

    ApplicationSubmit loadApplicationSubmitById(Integer applicationSubmitId);

    Integer deleteExamples(Integer id);

    /**
     * 获得选择题
     * @param practiceIds
     * @return
     */
    List<WarmupPractice> loadWarmupPractices(List<Integer> practiceIds);

    /**
     * 加载昨天的评论
     * @param warmupPractice
     * @return
     */
    List<WarmupPracticeDiscuss> loadYesterdayComments(WarmupPractice warmupPractice);

}
