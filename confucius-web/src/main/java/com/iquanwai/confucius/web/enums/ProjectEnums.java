package com.iquanwai.confucius.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum ProjectEnums {
    CORE_PROJECT(1,"核心能力项目"),
    MBA_PROJECTT(2,"商业进阶项目");

    private Integer projectId;
    private String projectName;

    public static ProjectEnums getById(Integer projectId) {
        for (ProjectEnums projectEnums : ProjectEnums.values()) {
            if (Objects.equals(projectId, projectEnums.getProjectId())) {
                return projectEnums;
            }
        }
        return null;
    }
}
