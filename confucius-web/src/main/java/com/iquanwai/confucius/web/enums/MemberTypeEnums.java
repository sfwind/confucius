package com.iquanwai.confucius.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum MemberTypeEnums {
    CORE_MEMBER(3,"核心能力项目"),
    THOUGHT_MEMBER(8,"商业进阶项目");

    private Integer memberTypeId;
    private String memberTypeName;

    public static MemberTypeEnums getById(Integer projectId) {
        for (MemberTypeEnums projectEnums : MemberTypeEnums.values()) {
            if (Objects.equals(projectId, projectEnums.getMemberTypeId())) {
                return projectEnums;
            }
        }
        return null;
    }
}
