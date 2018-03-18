package com.iquanwai.confucius.biz.po;

import lombok.Data;

@Data
public class TemplateMsg {

    private Integer id;

    /**
     * 模板消息id
     */
    private String messageId;
    /**
     * 备注
     */
    private String remark;

}
