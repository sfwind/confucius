package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.Data;

import java.util.Date;

/**
 * @author nethunder
 * @version 2017-11-01
 */
@Data
public class AuditionClassMember {
    private Integer id;
    private Integer profileId;
    private String openid;
    private String className;
    private Date addTime;
    private Date startDate;
    private Boolean active;
}
