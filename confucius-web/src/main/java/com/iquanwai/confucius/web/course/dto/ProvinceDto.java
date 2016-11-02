package com.iquanwai.confucius.web.course.dto;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/11/2.
 */
@Data
public class ProvinceDto {
    private List<RegionDto> province = Lists.newArrayList();
    private Map<Integer, List<RegionDto>> city = Maps.newHashMap();
}
