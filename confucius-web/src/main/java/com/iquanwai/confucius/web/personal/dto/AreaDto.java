package com.iquanwai.confucius.web.personal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by nethunder on 2017/2/4.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AreaDto {
    private String name;
    private List<AreaDto> sub;

    public AreaDto(String name){
        this.name = name;
    }
}
