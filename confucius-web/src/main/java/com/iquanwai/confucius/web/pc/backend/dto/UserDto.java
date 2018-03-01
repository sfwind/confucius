package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserDto {
    private String openid;
    private String nickname;
    private String industry;//行业
    private String function;//职业
    private String workingLife;//工作年限
    private String workingYear;//参加工作年份
    private String city;
    private String address;
    private String country;
    private String province;
    private String mobileNo;
    private String weixinId;
    private String realName;
    private String receiver;
    private String riseId;
    private String className;//班级
    private String groupId;//小组
    private String memberId;//学号
    private String memberType;
    private Date openDate;
}
