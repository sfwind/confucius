package com.iquanwai.confucius.biz.domain.weixin.message;

import com.iquanwai.confucius.biz.util.XMLHelper;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.w3c.dom.Document;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by justin on 17/7/8.
 */
@XmlRootElement(name="xml")
@Setter
@NoArgsConstructor
public class TextMessage {
    @XmlElement(name="ToUserName")
    public String toUserName;
    @XmlElement(name="FromUserName")
    public String fromUserName;
    @XmlElement(name="CreateTime")
    public Long createTime;
    @XmlElement(name="MsgType")
    public String msgType="text";
    @XmlElement(name="Content")
    public String content;

    public TextMessage(Document document){
        String createTime = XMLHelper.getNode(document, "CreateTime");
        this.createTime = Long.valueOf(createTime);
        this.fromUserName = XMLHelper.getNode(document, "FromUserName");
        this.toUserName = XMLHelper.getNode(document, "ToUserName");
        this.content = XMLHelper.getNode(document, "Content");
    }
}
