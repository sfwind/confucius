package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

/**
 * Created by nethunder on 2017/2/27.
 */
@Data
public class FragmentDailyData {
    private Integer problemCount; //'应用练习累计被选择数量',
    private Double dailyExpiredWarmCompleteRate; //'当日到期热身完成率',
    private Integer warmCompleteCount; //'热身总完成组数',
    private Double warmRightRate; //'热身平均正确率',
    private Integer warmCommentCount;//'问答评论总数',
    private Integer challengeSubmitCount;//'小目标提交数',
    private Integer challengeShowCount; //'小目标查看数',
    private Integer challengeCommentCount; //'小目标评论数',
    private Integer challengeVoteCount; //'小目标点赞数',
    private Integer applicationSubmitCount; //'应用练习提交数',
    private Double applicationCompleteRate; //'应用练习累计完成率',
    private Integer applicationShowCount; //'应用练习总浏览量',
    private Integer applicationCommentCount; //'应用评论总数',
    private Integer applicationVoteCount; // '应用点赞数',
}
