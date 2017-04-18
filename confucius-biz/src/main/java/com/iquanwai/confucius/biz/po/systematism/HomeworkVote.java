package com.iquanwai.confucius.biz.po.systematism;

import lombok.Data;

/**
 * Created by nethunder on 2017/1/2.
 */
@Data
public class HomeworkVote {
    private Integer id;
    private Integer referencedId;// '依赖的id',
    private Integer type; //1:挑战小目标,2:大作业',
    private String voteOpenId;//'谁点的赞',
    private Integer del;//'是否删除，1代表取消点赞',
}
