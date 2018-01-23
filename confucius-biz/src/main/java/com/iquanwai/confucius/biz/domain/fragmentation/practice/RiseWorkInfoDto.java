package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
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
    private List<String> picList;
    private Date publishTime;
    private Integer priority;
    private Integer role;
    private String signature;
    private Integer requestCommentCount;
    private Boolean request;

    public RiseWorkInfoDto(ApplicationSubmit origin) {
        this.submitId = origin.getId();
        this.type = Constants.PracticeType.APPLICATION;
        this.content = origin.getContent();
        this.request = origin.getRequestFeedback() && !origin.getFeedback();
        this.upTime = DateUtils.parseDateToString(origin.getPublishTime());
    }
}
