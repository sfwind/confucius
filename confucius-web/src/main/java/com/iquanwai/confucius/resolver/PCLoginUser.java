package com.iquanwai.confucius.resolver;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by nethunder on 2016/12/23.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PCLoginUser {
    private String openId;
    private LoginUser weixin;
    private String role;

    public static PCLoginUser defaultUser(){
        return new PCLoginUser(ConfigUtils.getDefaultOpenid(), LoginUser.defaultUser(), "student");
    }

}
