package com.iquanwai.confucius.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationTypeEnums {

    APPLICATION(11,"应用题"),
    GROUP_PRACTICE(13,"小组作业");

    private Integer code;

    private String msg;

}
