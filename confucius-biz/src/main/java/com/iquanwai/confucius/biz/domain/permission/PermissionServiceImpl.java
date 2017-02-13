package com.iquanwai.confucius.biz.domain.permission;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.permission.PermissionDao;
import com.iquanwai.confucius.biz.dao.permission.RoleDao;
import com.iquanwai.confucius.biz.po.permisson.Permission;
import com.iquanwai.confucius.biz.po.permisson.Role;
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

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String,List<Authority>> rolePermissions = Maps.newConcurrentMap();

    @PostConstruct
    @Override
    public void initPermission() {
        List<Role> roles = roleDao.loadAll(Role.class);
        logger.info("roles:{}",roles);
        roles.forEach(role->{
            List<Permission> permissions = permissionDao.loadPermissions(role.getId());
            logger.info("perrmission:{}",permissions);
            rolePermissions.put(role.getName(), permissions.stream().map(permission -> {
                Authority authority = new Authority();
                authority.setRoleId(role.getId());
                authority.setPermission(permission);
                try{
                    Pattern pattern = Pattern.compile(permission.getRegExUri());
                    authority.setPattern(pattern);
                } catch (PatternSyntaxException e){
                    logger.error("正则表达式异常,permission:{}",permission);
                    return null;
                }
                return authority;
            }).filter(item-> item.getPattern()!=null).collect(Collectors.toList()));
        });
    }

    @Override
    public List<Authority> loadPermissions(String roleName) {
        return rolePermissions.get(roleName);
    }

    @Override
    public Boolean checkPermission(String role, String uri) {
        List<Authority> permissions = this.loadPermissions(role);
        if(permissions==null){
            return false;
        } else {
            for(Authority permission:permissions){
                if(permission.getPattern().matcher(uri).matches()){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void reloadPermission(){
        rolePermissions.clear();
        initPermission();
    }

}
