package com.iquanwai.confucius.web.account.dto;

import lombok.Data;

/**
 * Created by 三十文
 */
@Data
public class UserInfo {

    private String nickName;
    private String avatarUrl;
    private Integer gender;
    private String city;
    private String province;
    private String country;
    private String language;

}
