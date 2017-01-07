package com.iquanwai.confucius.web.account.dto;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.resolver.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by nethunder on 2016/12/25.
 **/
@Data
public class AccountDto {
    private String openId;
    private LoginUser weixin;
    private String role;
    private CourseDto course;
}
