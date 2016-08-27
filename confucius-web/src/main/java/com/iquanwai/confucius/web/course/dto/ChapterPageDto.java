package com.iquanwai.confucius.web.course.dto;

import com.iquanwai.confucius.biz.dao.po.Page;
import lombok.Data;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class ChapterPageDto {
    private String openid;
    private String username;
    private boolean done;
    private Integer chapterId;
    private String chapterName;
    private String chapterPic;
    private Page page;
}
