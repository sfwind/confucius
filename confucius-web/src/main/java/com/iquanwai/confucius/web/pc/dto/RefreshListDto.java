package com.iquanwai.confucius.web.pc.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/3/7.
 */
@Data
public class RefreshListDto<T> {
    List<T> list;
    boolean end;
}