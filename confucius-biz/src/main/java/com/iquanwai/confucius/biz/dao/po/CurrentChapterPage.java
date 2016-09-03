package com.iquanwai.confucius.biz.dao.po;

import lombok.Data;

/**
 * Created by justin on 16/9/3.
 */
@Data
public class CurrentChapterPage {
    private int id;
    private String openid;
    private int chapterId;
    private int pageSequence;
}
