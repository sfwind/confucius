package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

@Data
public class AssistDto {
    private Integer id;
    private Integer roleId;
    private String roleName;
    private String riseId;
    private String nickName;
    private String headImageUrl;
    private String  reached;
    private String needVerified;
    private String upGrade;
}
