package com.iquanwai.confucius.biz.po.systematism;

import lombok.Data;

/**
 * Created by justin on 16/9/3.
 */
@Data
public class CurrentChapterPage {
    private int id;
    private String openid; //openid
    private Integer chapterId; //课程id
    private Integer pageSequence; //页码
}
