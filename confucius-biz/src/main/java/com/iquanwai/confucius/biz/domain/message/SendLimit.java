package com.iquanwai.confucius.biz.domain.message;

import lombok.Data;

/**
 * Created by nethunder on 2017/6/15.
 */
@Data
public class SendLimit {
    private Integer profileId;
    private Integer minSend; // 一分钟内发送的条数
    private Integer hourSend; // 一小时内发送的条数
    private Integer daySend; // 一天内发送的条数

}
