package com.iquanwai.confucius.web.pc.fragmentation.dto;

import com.iquanwai.confucius.web.course.dto.PictureDto;
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

}
