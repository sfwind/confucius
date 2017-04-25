package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

/**
 * Created by justin on 17/2/8.
 */
@Data
public class DiscussDto {
    private String comment;
    private Integer repliedId;
    private Integer warmupPracticeId;
}
