package com.iquanwai.confucius.biz.domain.weixin.message;

import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by justin on 17/7/8.
 */
@XmlRootElement(name="xml")
@Setter
@ToString
public class Message {
    @XmlElement(name="ToUserName")
    public String toUserName;
    @XmlElement(name="FromUserName")
    public String fromUserName;
    @XmlElement(name="CreateTime")
    public Long createTime;
    @XmlElement(name="MsgType")
    public String msgType="![CDATA[text]]";
    @XmlElement(name="Content")
    public String content;
}
