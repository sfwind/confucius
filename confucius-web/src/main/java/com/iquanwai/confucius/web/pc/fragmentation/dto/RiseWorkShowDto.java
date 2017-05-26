package com.iquanwai.confucius.web.pc.fragmentation.dto;

import com.iquanwai.confucius.biz.po.fragmentation.LabelConfig;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/1/15.
 */
@Data
public class RiseWorkShowDto {
    private String title; // 标题
    private String upName; // 用户名
    private String upTime; // 上传时间
    private String headImg; // 头像
    private String content; // 内容
    private Integer submitId; // 提交id
    private String type; // 点赞类型
    private Boolean isMine; // 是否是自己的
    private Integer voteCount; //  点赞数
    private Integer voteStatus;// 0 没有点赞，1 点赞中
    private Integer planId;  //训练计划id
    private Integer workId;
    private List<String> picList;
    private String signature; //签名
    private Integer role; //角色id
    private Integer requestCommentCount; //求点评次数
    private Boolean request; //是否已求点评
    private Integer knowledgeId; //知识点id

    private String desc; // 描述
    private List<LabelConfig> labelList;

}
