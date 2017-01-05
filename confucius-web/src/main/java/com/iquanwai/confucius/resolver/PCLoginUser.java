package com.iquanwai.confucius.resolver;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.po.permisson.Role;
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
    private List<Role> roles;
    private String role;
    private Long sleepTime;// 多久没有调用接口了，如果超过两小时则清理掉

    public static PCLoginUser defaultUser(){
        List<Role> roles = Lists.newArrayList();
        roles.add(new Role());
        return new PCLoginUser(ConfigUtils.getDefaultOpenid(), LoginUser.defaultUser(),roles, "student");
    }

}
