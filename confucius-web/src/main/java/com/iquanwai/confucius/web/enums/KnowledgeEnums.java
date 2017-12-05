package com.iquanwai.confucius.web.enums;

import lombok.Getter;

@Getter
public enum KnowledgeEnums {

    KNOWLEDG_Duplicate_ERROR(-1,"设置的章节与现有章节重复"),
    UNKNOWN_ERROR(-100,"未知错误");

    private Integer code;

    private String msg;

    KnowledgeEnums(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
