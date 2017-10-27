package com.iquanwai.confucius.biz.domain.weixin.pay;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by justin on 2017/10/25.
 */
@XmlRootElement(name="xml")
@Data
public class RefundOrderReply {
    private String return_code; //返回状态码
    private String return_msg; //返回信息
    private String result_code; //业务结果
    private String err_code; //错误代码
    private String err_code_des; //错误代码描述
}
