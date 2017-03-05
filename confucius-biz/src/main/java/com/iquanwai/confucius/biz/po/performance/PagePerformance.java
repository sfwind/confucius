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
    private int cookieSize;
    private int redirect;
    private int dns;
    private int connect;
    private int network;
    private int send;
    private int receive;
    private int backend;
    private int render;
    private int dom;
    private int frontend;
    private int load;
    private int domReady;
    private int interactive;
    private int ttf;
    private int ttr;
    private int ttdns;
    private int ttconnect;
    private int ttfb;
    private int firstPaint;
    private Date addTime;
}
