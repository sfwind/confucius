package com.iquanwai.confucius.biz.domain.fragmentation.plan;

import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/12/11.
 */
@Data
public class Practice {
    private Knowledge knowledge;
    private Integer type;
    private Integer status;
    private Boolean unlocked;
    private List<Integer> practiceIdList;
    private Integer series;
    private Integer sequence;
}
