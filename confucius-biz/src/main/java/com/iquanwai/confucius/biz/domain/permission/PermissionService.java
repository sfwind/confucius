package com.iquanwai.confucius.biz.domain.permission;

import com.iquanwai.confucius.biz.po.common.permisson.Role;

import java.util.List;

/**
 * Created by nethunder on 2016/12/28.
 */
public interface PermissionService {
    /**
     * 初始化权限信息
     */
    void initPermission();

    /**
     * 获取角色的所有权限
     * */
    List<Authority> loadPermissions(Integer roleLevel);

    /**
     * 根据角色id,校验权限
     * */
    Boolean checkPermission(Integer roleId, String regExUri);

    /**
     * 重新加载权限
     * */
    void reloadPermission();

    /**
     * 获取用户的角色
     * */
    Role getRole(Integer profileId);

    /**
     * 获取用户的角色
     * */
    Role getRole(String openid);

}
