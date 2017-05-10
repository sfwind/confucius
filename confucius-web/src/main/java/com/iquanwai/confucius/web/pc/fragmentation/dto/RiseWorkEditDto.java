package com.iquanwai.confucius.web.pc.fragmentation.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/1/15.
 */
@Data
public class RiseWorkEditDto {
    private String title;
    private String description;// "图文混排内容", //html
    private String content;// "balbal" //提交内容
    private List<String> picList;
    private Integer submitId;
    private Integer moduleId;
    private Integer requestCommentCount;
    private Boolean request; //是否已求点评

}
