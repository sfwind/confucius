package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

/**
 * Created by nethunder on 2017/3/10.
 */
@Data
public class LabelConfig {
    private Integer id;
    private Integer problemId;
    private String name;
    private Boolean Del;

    private Boolean selected;
}
