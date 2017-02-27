package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

/**
 * Created by nethunder on 2017/2/27.
 */
@Data
public class FragmentDailyData {
    private Integer problemCount; //'应用训练累计被选择数量',
    private Double dailyExpiredWarmCompleteRate; //'当日到期热身完成率',
    private Integer warmCompleteCount; //'热身总完成组数',
    private Double warmRightRate; //'热身平均正确率',
    private Integer warmCommentCount;//'问答评论总数',
    private Integer challengeSubmitCount;//'专题训练提交数',
    private Integer challengeShowCount; //'专题训练查看数',
    private Integer challengeCommentCount; //'挑战评论数',
    private Integer challengeVoteCount; //'挑战点赞数',
    private Integer applicationSubmitCount; //'应用训练提交数',
    private Double applicationCompleteRate; //'应用训练累计完成率',
    private Integer applicationShowCount; //'应用训练总浏览量',
    private Integer applicationCommentCount; //'应用评论总数',
    private Integer applicationVoteCount; // '应用点赞数',
}
