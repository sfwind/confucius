package com.iquanwai.confucius.web.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nethunder on 2016/12/15.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PictureDto {
    private Integer moduleId;
    private Integer referencedId;
    private String picSrc;
}
