package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

/**
 * 课前思考
 */
@Data
public class ProblemPreview {


    private Integer id;

    /**
     * 内容
     */
    private String description;
    /**
     * 音频id
     */
    private Integer audioId;
    /**
     * 视频id
     */
    private Integer videoId;

    private Integer problemScheduleId;
    /**
     * 是否更新（0-未更新 1-更新 2-插入）
     */
    private Integer updated;
}
