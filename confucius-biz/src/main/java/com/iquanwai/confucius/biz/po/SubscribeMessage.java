package com.iquanwai.confucius.biz.po;

import lombok.Data;

/**
 * Created by justin on 17/7/8.
 */
@Data
public class SubscribeMessage {
    private int id;
    private String message; //消息或者媒体id
    private Integer type; //类型（1-文字，2-图片，3-语音）
    private Integer event; //类型（1-关注，2-扫码）
    private String channel; //渠道
}
