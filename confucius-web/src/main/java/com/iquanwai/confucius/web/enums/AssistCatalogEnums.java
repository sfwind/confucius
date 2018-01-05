package com.iquanwai.confucius.web.enums;

import lombok.Getter;


/**
 * 助教级别
 */
@Getter
public enum AssistCatalogEnums {

    PROBATIONARY_ASSIST(3,"见习"),
    ASSIST(4,"教练"),
    CANDIDATE_ASSIST(11,"候选"),
    EXPIRED_ASSIST(0,"过期")
    ;

    private Integer roleId;
    private String roleName;

    AssistCatalogEnums(Integer roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }
}
