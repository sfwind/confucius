package com.iquanwai.confucius.web.pc.dto;

import com.iquanwai.confucius.web.course.dto.PictureDto;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/1/2.
 */
@Data
public class ChallengeShowDto {
    private String title;
    private String upName;
    private String upTime;
    private String headImg;
    private String content;
    private Integer submitId;
    private String type;
    private Boolean isMine;
    private Integer problemId;
    private Integer voteCount;
    private Integer voteStatus;// 0 没有点赞，1 点赞中
    private Integer planId;
    private Integer challengeId;
    private List<PictureDto> picList;

}
