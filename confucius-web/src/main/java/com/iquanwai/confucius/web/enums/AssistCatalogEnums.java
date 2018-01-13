package com.iquanwai.confucius.web.enums;

import lombok.Getter;

import java.util.Objects;


/**
 * 助教级别
 */
@Getter
public enum AssistCatalogEnums {

    PROBATIONARY_ASSIST(3, "见习", 2),
    ASSIST(4, "教练", 3),
    CANDIDATE_ASSIST(11, "候选", 1),
    EXPIRED_ASSIST(0, "过期", -1);

    private Integer roleId;
    private String roleName;
    private Integer level;

    AssistCatalogEnums(Integer roleId, String roleName, Integer level) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.level = level;
    }

    public static AssistCatalogEnums getById(Integer roleId) {
        for (AssistCatalogEnums assistCatalogEnums : AssistCatalogEnums.values()) {
            if (Objects.equals(roleId, assistCatalogEnums.getRoleId())) {
                return assistCatalogEnums;
            }
        }
        return null;
    }
}
