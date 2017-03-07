package com.iquanwai.confucius.biz.domain.permission;

import com.iquanwai.confucius.biz.po.common.permisson.Permission;
import lombok.Data;

import java.util.regex.Pattern;

/**
 * Created by nethunder on 2016/12/28.
 */
@Data
public class Authority {
    private Integer roleId;
    private Permission permission;
    private Pattern pattern;
}
