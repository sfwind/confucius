package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 17/3/4.
 */
@Data
public class ProblemSchedule  {
    private int id;
    private Integer problemId; //小课id
    private Integer section; //小课第几节
    private Integer knowledgeId; //知识点id
    private Integer chapter; //小课第几章
    private Integer series; //序号


    /**
     * 非 db 字段
     */
    private List<Knowledge> knowledges; // 知识点集合

}

