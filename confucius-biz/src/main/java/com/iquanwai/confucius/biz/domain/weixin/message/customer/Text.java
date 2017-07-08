package com.iquanwai.confucius.biz.domain.weixin.message.customer;

import lombok.Data;

/**
 * Created by justin on 17/7/8.
 */
@Data
public class Text {
    private String content;

    public Text(String content) {
        this.content = content;
    }
}
