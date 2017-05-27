package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPractice;
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
    List<ApplicationSubmit> loadApplicationSubmit(Integer practiceId, Page page);

    /**
     * 获取48小时内讨论区活跃的问题
     */
    List<WarmupPractice> getLastTwoDayActivePractice();

    /**
     * 获取巩固练习
     * @param practiceId 练习id
     */
    WarmupPractice getWarmupPractice(Integer practiceId);

    /**
     * 内容运营回复某个巩固练习
     *
     * @param openid           发表讨论的用户openid
     * @param warmupPracticeId 巩固练习id
     * @param comment          讨论内容
     * @param repliedId        回复的讨论id
     */
    void discuss(String openid, Integer warmupPracticeId, String comment, Integer repliedId);

    /**
     * 回复加精
     * @param discussId 讨论id
     */
    void highlightDiscuss(Integer discussId);

    /**
     * 提交作业加精
     * @param practiceId 练习id
     * @param submitId 提交id
     */
    void highlightApplicationSubmit(Integer practiceId, Integer submitId);


    /**
     * 作业是否已评论
     * @param submitId 提交id
     * @param commentOpenid 评论者openid
     */
    boolean isComment(Integer submitId, String commentOpenid);

    /**
     * 获取小课的所有巩固练习
     * @param problemId 小课id
     */
    List<WarmupPractice> getPracticeByProblemId(Integer problemId);

    /**
     * 保存巩固练习
     * @param warmupPractice 巩固练习
     */
    void save(WarmupPractice warmupPractice);

    /**
     * 获取下一个巩固练习
     * @param problemId 小课id
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
    Pair<Integer, String> deleteAsstWarmupDiscuss(Integer discussid);

}
