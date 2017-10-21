package com.iquanwai.confucius.web.pc.fragmentation.dto;

import com.iquanwai.confucius.web.course.dto.PictureDto;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/1/14.
 */
@Data
public class ApplicationDto {
    private Integer id; // 应用练习id
    private String topic; //任务标题
    private String description; // 题干
    private Integer knowledgeId; //知识点id
    private Integer sceneId; //子场景id
    private Integer difficulty; //难易度（1-容易，2-普通，3-困难）
    private Boolean submitted; //是否提交过 非db字段
    private String content; //提交内容 非db字段
    private Integer submitId; // 提交id，非db字段;
    private Date submitUpdateTime;// 非db字段
    private Integer planId; // 计划id

    private List<PictureDto> picList;
    private Integer moduleId;
    private String headImg;
    private String upName;
    private String upTime;
    private Integer voteCount;
    private Boolean canVote;

}
