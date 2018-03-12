package com.iquanwai.confucius.biz.domain.permission;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.common.permission.PermissionDao;
import com.iquanwai.confucius.biz.dao.common.permission.RoleDao;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.po.common.permisson.Permission;
import com.iquanwai.confucius.biz.po.common.permisson.Role;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2016/12/29.
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private RoleDao roleDao;
    @Autowired
    private PermissionDao permissionDao;
    @Autowired
    private UserRoleDao userRoleDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, List<Authority>> rolePermissions = Maps.newConcurrentMap();

    private List<String> uriPatterns = Lists.newArrayList();

    @PostConstruct
    @Override
    public void initPermission() {
        List<Role> roles = roleDao.loadAll(Role.class);
        logger.info("roles:{}", roles);
        roles.forEach(role -> {
            List<Permission> permissions = permissionDao.loadPermissions(role.getLevel());
            logger.info("permission:{} for role {}", permissions, role.getName());
            rolePermissions.put(role.getId(), permissions.stream().map(permission -> {
                Authority authority = new Authority();
                authority.setRoleId(role.getId());
                authority.setPermission(permission);
                uriPatterns.add(permission.getRegExUri());
                try {
                    Pattern pattern = Pattern.compile(permission.getRegExUri());
                    authority.setPattern(pattern);
                } catch (PatternSyntaxException e) {
                    logger.error("正则表达式异常,permission:{}", permission);
                    return null;
                }
                return authority;
            }).filter(item -> item.getPattern() != null).collect(Collectors.toList()));
        });
    }

    @Override
    public List<Authority> loadPermissions(Integer roleId) {
        return rolePermissions.get(roleId);
    }

    @Override
    public Boolean checkPermission(Integer roleId, String uri) {
        for (String regExUri : uriPatterns) {
            try {
                Pattern pattern = Pattern.compile(regExUri);
                // 如果url有权限控制,查看用户有没有权限
                if (pattern.matcher(uri).matches()) {
                    List<Authority> permissions = this.loadPermissions(roleId);
                    if (permissions == null) {
                        logger.error("roleId:{} don't have permission: {}", roleId, uri);
                        return false;
                    }else{
                        // 遍历用户的权限,如果用户有相关权限,返回true,反之返回false
                        for (Authority permission : permissions) {
                            if (permission.getPattern().matcher(uri).matches()) {
                                return true;
                            }
                        }

                        return false;
                    }
                }
            } catch (PatternSyntaxException e) {
                logger.error("正则表达式异常:{}", regExUri);
                return false;
            }
        }

        return true;
    }

    @Override
    public void reloadPermission() {
        rolePermissions.clear();
        initPermission();
    }

    @Override
    public Role getRole(Integer profileId) {
        List<UserRole> userRoles = userRoleDao.getRoles(profileId);
        if (CollectionUtils.isEmpty(userRoles)) {
            return null;
        } else {
            Integer roleId = userRoles.get(0).getRoleId();
            return roleDao.load(Role.class, roleId);
        }
    }

}
