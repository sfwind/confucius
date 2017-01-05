package com.iquanwai.confucius.biz.po;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class Page {
    private int id;
    private Integer chapterId; //章节id
    private Integer sequence;  //章节内顺序
    private String topic; //页标题
    private List<Material> materialList;
    test
}

