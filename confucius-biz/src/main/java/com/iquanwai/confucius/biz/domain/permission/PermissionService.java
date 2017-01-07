package com.iquanwai.confucius.biz.domain.permission;

import com.iquanwai.confucius.biz.po.permisson.Permission;

import java.util.List;

/**
 * Created by nethunder on 2016/12/28.
 */
public interface PermissionService {
    /**
     * 初始化权限信息
     */
    void initPermission();

    List<Authority> loadPermissions(String roleName);

    Boolean checkPermission(String role, String regExUri);

    void reloadPermission();

    /**
     * 检查是否是我们的学员
     */
    Boolean isFragmentStudent(String openId);
}
