package com.iquanwai.confucius.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KnowledgeEnums {

    KNOWLEDG_Duplicate_ERROR(-2,"设置的章节与现有章节重复"),
    UNKNOWN_ERROR(-100,"未知错误");

    private Integer code;

    private String msg;
}
