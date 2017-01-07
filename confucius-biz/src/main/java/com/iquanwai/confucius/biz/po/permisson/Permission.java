package com.iquanwai.confucius.biz.po.permisson;

import lombok.Data;
import lombok.ToString;

/**
 * Created by nethunder on 2016/12/28.
 */
@Data
@ToString
public class Permission {
    private Integer id;
    private Integer roleId;
    private String regExUri;
}
