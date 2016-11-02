package com.iquanwai.confucius.web.course.dto;

import lombok.Data;

/**
 * Created by justin on 16/11/2.
 */
@Data
public class RegionDto {
    private Integer id;
    private String name;

    public RegionDto id(Integer id){
        this.id = id;
        return this;
    }

    public RegionDto name(String name){
        this.name = name;
        return this;
    }
}
