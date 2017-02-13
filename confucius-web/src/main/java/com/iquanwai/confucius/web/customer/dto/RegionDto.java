package com.iquanwai.confucius.web.customer.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/2/6.
 */
@Data
public class RegionDto {
    private List<AreaDto> provinceList;
    private List<AreaDto> cityList;

}
