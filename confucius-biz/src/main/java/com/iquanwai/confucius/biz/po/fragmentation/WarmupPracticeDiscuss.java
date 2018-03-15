package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/2/8.
 */
@Data
public class WarmupPracticeDiscuss extends AbstractComment{
    private Integer warmupPracticeId; //巩固练习id
    private Integer originDiscussId; //讨论最早发起的评论id
}
