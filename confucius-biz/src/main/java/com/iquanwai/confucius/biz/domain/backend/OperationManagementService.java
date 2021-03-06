package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.page.Page;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by justin on 17/3/16.
 */
public interface OperationManagementService {

    /**
     * 根据应用id,获取应用练习提交记录
     * @param practiceId 练习id
     * @param page 分页对象
     */
    List<ApplicationSubmit> loadApplicationSubmit(Integer practiceId, Page page,String show);

    /**
     * 获取60天内讨论区活跃的问题
     */
    List<WarmupPractice> getLastSixtyDayActivePractice(Page page);

    /**
     * 获取巩固练习
     * @param practiceId 练习id
     */
    WarmupPractice getWarmupPractice(Integer practiceId);

    WarmupPractice getTargetPractice(Integer practiceId,List<WarmupPracticeDiscuss> warmupPracticeDiscuss);
    /**
     * 内容运营回复某个巩固练习
     *
     * @param profileId           发表讨论的用户id
     * @param warmupPracticeId 巩固练习id
     * @param comment          讨论内容
     * @param repliedId        回复的讨论id
     */
    void discuss(Integer profileId, Integer warmupPracticeId, String comment, Integer repliedId);

    /**
     * 回复加精
     * @param discussId 讨论id
     */
    void highlightDiscuss(Integer discussId);

    /**
     * 回复取消加精
     * @param discussId 讨论id
     */
    void unhighlightDiscuss(Integer discussId);

    /**
     * 提交作业加精
     * @param submitId 提交id
     */
    void highlightApplicationSubmit(Integer submitId);

    /**
     * 提交作业取消加精
     * @param submitId 提交id
     */
    void unhighlightApplicationSubmit(Integer submitId);

    /**
     * 作业是否已评论
     * @param submitId 提交id
     * @param commentProfileId 评论者id
     */
    boolean isComment(Integer submitId, Integer commentProfileId);

    /**
     * 获取课程的所有选择题
     * @param problemId 课程id
     */
    List<WarmupPractice> getPracticeByProblemId(Integer problemId);

    /**
     * 保存选择题
     * @param warmupPractice 选择题
     */
    void save(WarmupPractice warmupPractice);

    /**
     * 获取下一个选择题
     * @param problemId 课程id
     * @param prePracticeId 练习id
     */
    WarmupPractice getNextPractice(Integer problemId, Integer prePracticeId);

    /**
     * 删除巩固练习下的教练评论
     * 返回left值
     *  1：删除成功
     *  0：非教练评价
     * -1：数据异常
     */
    Integer deleteAsstWarmupDiscuss(Integer discussId);




}
