package com.iquanwai.confucius.biz.domain.message;

import lombok.Data;

/**
 * Created by nethunder on 2017/6/16.
 */
@Data
public class SMSSendResult {
    private String msgid;
    private String result;
    private String desc;
    private String failPhones;
    private String status;
}
