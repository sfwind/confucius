package com.iquanwai.confucius.biz.domain.weixin.message.callback;

import com.iquanwai.confucius.biz.util.XMLHelper;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by justin on 17/7/9.
 */
@Setter
@ToString
@NoArgsConstructor
public class Voice {
    @XmlElement(name="MediaId")
    public String media_id;

    public Voice(String mediaId) {
        this.media_id = XMLHelper.appendCDATA(mediaId);
    }
}
