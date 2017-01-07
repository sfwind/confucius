package com.iquanwai.confucius.web.pc.dto;

import com.iquanwai.confucius.biz.po.Picture;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengePractice;
import com.iquanwai.confucius.web.course.dto.PictureDto;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2016/12/27.
 */
@Data
public class ChallengeDto {
    private Integer id; // 挑战任务id
    private String description;// "图文混排内容", //html
    private String pic;// "http://someurl",  //图片url
    private Integer problemId;//问题id
    private String pcurl;//"http://someurl", //pc端url
    private Boolean submitted;//true, //是否提交过
    private String content;// "balbal" //提交内容
    private List<PictureDto> picList;
    private Integer submitId;
    private Integer moduleId;
    private String submitUrl;
    private String headImg;
    private String upName;
    private String upTime;
    private Integer voteCount;
    private Boolean canVote;
    private Integer planId;

    public static ChallengeDto getFromPo(ChallengePractice param){
        if(param==null){
            return null;
        }
        ChallengeDto result = new ChallengeDto();
        result.setId(param.getId());
        result.setDescription(param.getDescription());
        result.setPic(param.getPic());
        result.setProblemId(param.getProblemId());
        result.setPcurl(param.getPcurl());
        result.setSubmitId(param.getSubmitId());
        result.setSubmitted(param.getSubmitted());
        result.setContent(param.getContent());
        result.setSubmitUrl(param.getSubmitUrl());
        return result;
    }
}
