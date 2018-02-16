package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

/**
 * Created by 三十文 on 2017/9/15
 */
@Data
public class MonthlyCampDto {

    private Integer riseClassMemberId;
    private String headImgUrl; // 头像
    private String nickName; // 昵称
    private String riseId; // riseId
    private String className; // 班级名称
    private String classNameStr; // 中文班级名称
    private String groupId; // 小组名称
    private String memberId; // 学号
    private Integer active; // 是否有效
    private String activeStr; // 学习中


    private String tips; // 前端编辑备注

    @Override
    public String toString() {
        return "ClassName:" + className + ", GroupId:" + groupId + ", Active:" + active;
    }

}
