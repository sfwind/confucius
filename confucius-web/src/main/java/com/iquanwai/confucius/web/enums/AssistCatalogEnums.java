package com.iquanwai.confucius.web.enums;

import lombok.Getter;

import java.util.Objects;


/**
 * 助教级别
 */
@Getter
public enum AssistCatalogEnums {
    III_HIGH_ASSIST(17,"高级3教练",8),
    II_HIGH_ASSIST(16,"高级2教练",7),
    I_HIGH_ASSIST(15,"高级1教练",6),
    III_ASSIST(14,"III级教练",5),
    II_ASSIST(13,"II级教练",4),
    I_ASSIST(12,"I级教练",3),
    PROBATIONARY_ASSIST(3,"见习",2),
    CANDIDATE_ASSIST(11,"候选",1),
    EXPIRED_ASSIST(0,"过期",-1);

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
