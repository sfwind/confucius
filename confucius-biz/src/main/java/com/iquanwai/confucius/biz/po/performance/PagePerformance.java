package com.iquanwai.confucius.biz.po.performance;

import lombok.Data;

import java.util.Date;

/**
 * Created by yongqiang.shen on 2017/3/2.
 */
@Data
public class PagePerformance {
    private int id;
    private String app;
    private String url;
    private String screen;
    private String viewport;
    private String uuid;
    private Integer cookieSize;
    private Integer redirect;
    private Integer dns;
    private Integer connect;
    private Integer network;
    private Integer send;
    private Integer receive;
    private Integer backend;
    private Integer render;
    private Integer dom;
    private Integer frontend;
    private Integer load;
    private Integer domReady;
    private Integer interactive;
    private Integer ttf;
    private Integer ttr;
    private Integer ttdns;
    private Integer ttconnect;
    private Integer ttfb;
    private Date addTime;

}
