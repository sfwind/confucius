package com.iquanwai.confucius.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/9/3.
 */
@Data
public class CurrentChapterPage {
    private int id;
    private String openid; //openid
    private int chapterId; //课程id
    private int pageSequence; //页码
}
