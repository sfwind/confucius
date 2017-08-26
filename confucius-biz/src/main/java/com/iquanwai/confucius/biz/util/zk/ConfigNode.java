package com.iquanwai.confucius.biz.util.zk;

import lombok.Data;

/**
 * Created by justin on 17/4/13.
 */
@Data
public class ConfigNode{
    private String value;
    private long c_time;
    private long m_time;

    private String key;
    private String desc;

}
