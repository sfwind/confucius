package com.iquanwai.confucius.biz.po.fragmentation;


import lombok.Data;

import java.util.Date;

/**
 * 用户推荐表
 */
@Data
public class UserRecommedation {
    private Integer id;
    private Integer profileId;
    private String friendOpenid;
    private boolean del;
    private Integer action;
    private Date addTime;
    private Date updateTime;
}
