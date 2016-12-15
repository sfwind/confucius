package com.iquanwai.confucius.web.course.dto;

import com.iquanwai.confucius.biz.po.Homework;
import com.iquanwai.confucius.biz.po.Picture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by nethunder on 2016/12/15.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomeworkLoadDto {
    private Integer moduleId;
    private Integer submitId;
    private Homework homework;
    private List<PictureDto> picList;
}
