package com.iquanwai.confucius.biz.po;

import lombok.Data;

/**
 * Created by justin on 17/7/8.
 */
@Data
public class SubscribeMessage {
    private int id;
    private String message;
    private Integer type;
    private String channel;
}
