package com.iquanwai.confucius.web.pc.fragmentation.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/1/11.
 */
@Data
public class ProblemListDto {
    private Integer id;// 问题id
    private String problem;// 问题
    private Integer status; // 1 进行中  2  已完成  3 已过期 -1 未解锁
    private Boolean del; //是否删除
}
