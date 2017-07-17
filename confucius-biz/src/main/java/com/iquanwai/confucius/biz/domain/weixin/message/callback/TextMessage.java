package com.iquanwai.confucius.biz.domain.weixin.message.callback;

import com.iquanwai.confucius.biz.util.XMLHelper;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by justin on 17/7/8.
 */
@XmlRootElement(name="xml")
@ToString
@NoArgsConstructor
public class TextMessage {
    @XmlElement(name="ToUserName")
    private String toUserName;
    @XmlElement(name="FromUserName")
    private String fromUserName;
    @XmlElement(name="CreateTime")
    private Long createTime;
    @XmlElement(name="MsgType")
    private String msgType="<![CDATA[text]]>";
    @XmlElement(name="Content")
    private String content;

    public TextMessage(String wxid, String openid, String replyMessage) {
        this.content = XMLHelper.appendCDATA(replyMessage);
        this.createTime = System.currentTimeMillis() / 1000;
        this.fromUserName = XMLHelper.appendCDATA(wxid);
        this.toUserName = XMLHelper.appendCDATA(openid);
    }
}
