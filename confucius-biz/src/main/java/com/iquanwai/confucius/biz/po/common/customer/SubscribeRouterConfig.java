package com.iquanwai.confucius.biz.po.common.customer;

import lombok.Data;

@Data
public class SubscribeRouterConfig {

    private Integer id;
    private String url; // 正则匹配 url
    private String scene; // 场景号
    private Integer sequence; // 正则匹配优先级
    private Boolean del;

    public static final String QUERY_KEY="_fk";
}
