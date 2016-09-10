package com.iquanwai.confucius.web.course.dto;

import com.iquanwai.confucius.biz.po.Page;
import lombok.Data;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class ChapterPageDto {
    private Integer chapterId;
    private String chapterName;
    private String chapterPic;
    private Integer chapterType;
    private Page page;
}
