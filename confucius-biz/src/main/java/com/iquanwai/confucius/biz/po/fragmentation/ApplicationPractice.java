package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

/**
 * Created by nethunder on 2017/1/13.
 */
@Data
public class ApplicationPractice {
    private int id;
    private String topic; //任务标题
    private String description; // 题干
    private Integer knowledgeId; //知识点id
    private Integer sceneId; //子场景id
    private Integer difficulty; //难易度（1-容易，2-普通，3-困难）
    private Integer problemId; //课程id

    //新增字段（2017/11/22）
    private  Integer sequence;//顺序
    private String pic;//图片链接
    private String practiceUid;//任务编号
    private int updated;//是否修改

    /**
     * 应用题类型
     */
    private Integer type;
}
