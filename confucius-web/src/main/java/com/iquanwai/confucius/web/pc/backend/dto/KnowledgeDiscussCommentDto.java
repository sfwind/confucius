package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by 三十文
 */
@Data
public class KnowledgeDiscussCommentDto {

    // 准备回复的知识点评论
    private KnowledgeDiscussDto discuss;
    // 针对该笔知识点评论的所有回复内容
    private List<KnowledgeDiscussDto> discussReplies;

}
