package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

@Data
public class RiseClassMember {

    private Integer id;
    private String classId; // classId 2017 年份
    private String groupId; // 班级 01
    private String memberId;
    private Integer profileId; // 用户 id
    private Integer active; // 是否参与本次学习

}
