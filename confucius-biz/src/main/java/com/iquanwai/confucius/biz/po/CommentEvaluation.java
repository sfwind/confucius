package com.iquanwai.confucius.biz.po;

import lombok.Data;

@Data
public class CommentEvaluation {

    private Integer id;
    private Integer commentId;
    private Integer useful;
    private String reason;
    private Integer evaluated;

}
