package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserDto {
    private String openid;
    private String nickname;
    private String city;
    private String address;
    private String country;
    private String province;
    private String mobileNo;
    private String weixinId;
    private String realName;
    private String receiver;
    private String married;
    private String riseId;
    private String memberId;
    private Integer memberType;
    private Date openDate;
}
