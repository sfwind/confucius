package com.iquanwai.confucius.biz.po.common.message;

import lombok.Data;

/**
 * Created by nethunder on 2017/8/10.
 */
@Data
public class CustomerMessageLog {

    private Integer id;
    private String openId;
    private String publishTime;
    private String comment;
    private String contentHash; // 发送内容的 hash 值
    private Integer forwardlyPush; // 用户无触发推送
    private Integer validPush; // 有效的推送

}
