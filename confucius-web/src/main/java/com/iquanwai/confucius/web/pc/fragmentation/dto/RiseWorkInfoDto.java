package com.iquanwai.confucius.web.pc.fragmentation.dto;

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

    public RiseWorkInfoDto(SubjectArticle origin){
        this.title = origin.getTitle();
        this.submitId = origin.getId();
        this.type = Constants.PracticeType.SUBJECT;
        this.content = origin.getContent();
        this.voteCount = origin.getVoteCount();
        this.upTime = DateUtils.parseDateToString(origin.getUpdateTime());
        this.commentCount = origin.getCommentCount();
        this.perfect = origin.getSequence() != null && origin.getSequence() > 0;
        this.authorType = origin.getAuthorType();
        this.title = origin.getTitle();
    }
}
