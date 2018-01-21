package com.iquanwai.confucius.biz.po.common.permisson;

import lombok.Data;

/**
 * Created by nethunder on 2016/12/28.
 */
@Data
public class UserRole {
    private Integer id;
    private Integer roleId;
    private Integer profileId;
    private Boolean del;

    // 非DB字段
    private String asstName;
    private String asstType;
    private Integer level;
}
