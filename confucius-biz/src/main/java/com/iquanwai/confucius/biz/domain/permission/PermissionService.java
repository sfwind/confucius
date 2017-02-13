package com.iquanwai.confucius.biz.domain.permission;

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

}
