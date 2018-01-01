package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/3/7.
 */
@Data
public class RefreshListDto<T> {
    private List<T> list;
    private List<T> highlightList;
    private boolean end;
}
