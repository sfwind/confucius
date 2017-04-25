package com.iquanwai.confucius.web.pc.fragmentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by nethunder on 2017/1/3.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedirectRouteDto {
    private String pathName; // 需要跳转到的pathname
    private Map<String,Object> query; // 参数
}
