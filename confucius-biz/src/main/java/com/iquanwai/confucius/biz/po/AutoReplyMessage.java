package com.iquanwai.confucius.biz.po;

import lombok.Data;

/**
 * Created by justin on 17/7/9.
 */
@Data
public class AutoReplyMessage {
    private int id;
    private String message; //消息或者媒体id
    private Integer type; //类型（1-文字，2-图片，3-语音）
    private String keyword; //关键字
}
