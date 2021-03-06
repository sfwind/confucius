package com.iquanwai.confucius.biz.po.systematism;

import lombok.Data;

/**
 * Created by nethunder on 2017/1/2.
 */
@Data
public class HomeworkVote {
    private Integer id;
    private Integer referencedId;// 依赖的id
    private Integer type; //1:小目标,2:应用练习
    private Integer voteProfileId; //点赞人id
    private Integer del;//是否删除，1代表取消点赞
    private Integer votedProfileId; //被点赞人id
    private Integer device; //设备
}
