package com.iquanwai.confucius.web.pc.asst.dto;

import lombok.Data;

import java.util.List;

/**
 * 返回给前端的className和GroupId
 */
@Data
public class ClassNameGroups {

    private List<String> className;
    private List<Group> groupIds;
}
