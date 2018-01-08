package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/1/20.
 */
@Data
public class RiseWorkCommentListDto {
    private List<RiseWorkCommentDto> list;
    private Integer count;
}
