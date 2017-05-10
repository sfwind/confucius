package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.LabelConfig;
import com.iquanwai.confucius.biz.po.fragmentation.SubjectArticle;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/1/14.
 */
@Data
@NoArgsConstructor
public class RiseWorkInfoDto {
    private String title;
    private String upName;
    private String upTime;
    private String headPic;
    private String content;
    private Integer voteCount;
    private Integer commentCount;
    private Integer submitId;
    private Integer type;

    private Boolean perfect;
    private Integer authorType;
    private Boolean isMine;
    private List<LabelConfig> labelList;
    private List<String> picList;
    private Date publishTime;
    private Integer priority;
    private Integer role;
    private String signature;
    private Integer requestCommentCount;
    private Boolean request;

    public RiseWorkInfoDto(SubjectArticle origin){
        this.title = origin.getTitle();
        this.submitId = origin.getId();
        this.type = Constants.PracticeType.SUBJECT;
        this.content = origin.getContent();
        this.voteCount = origin.getVoteCount();
        this.upTime = DateUtils.parseDateToString(origin.getUpdateTime());
        this.commentCount = origin.getCommentCount();
        this.request = origin.getRequestFeedback();
        this.perfect = origin.getSequence() != null && origin.getSequence() > 0;
        this.authorType = origin.getAuthorType();
    }

    public RiseWorkInfoDto(ApplicationSubmit origin){
        this.submitId = origin.getId();
        this.type = Constants.PracticeType.APPLICATION;
        this.content = origin.getContent();
        this.request = origin.getRequestFeedback();
        this.upTime = DateUtils.parseDateToString(origin.getUpdateTime());
    }
}
