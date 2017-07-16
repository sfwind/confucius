package com.iquanwai.confucius.biz.domain.weixin.message.callback;

import com.iquanwai.confucius.biz.util.XMLHelper;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by justin on 17/7/9.
 */
@XmlRootElement(name="xml")
@ToString
@NoArgsConstructor
public class VoiceMessage {
    @XmlElement(name="ToUserName")
    private String toUserName;
    @XmlElement(name="FromUserName")
    private String fromUserName;
    @XmlElement(name="CreateTime")
    private Long createTime;
    @XmlElement(name="MsgType")
    private String msgType="<![CDATA[image]]>";
    @XmlElement(name="Voice")
    private Voice voice;

    public VoiceMessage(String wxid, String openid, String mediaid) {
        this.voice = new Voice(mediaid);
        this.createTime = System.currentTimeMillis() / 1000;
        this.fromUserName = XMLHelper.appendCDATA(wxid);
        this.toUserName = XMLHelper.appendCDATA(openid);
    }

    @Setter
    @ToString
    @NoArgsConstructor
    public static class Voice {
        @XmlElement(name="MediaId")
        public String media_id;

        public Voice(String mediaId) {
            this.media_id = XMLHelper.appendCDATA(mediaId);
        }
    }
}
