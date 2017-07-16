package com.iquanwai.confucius.biz.domain.weixin.message.callback;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.po.GraphicMessage;
import com.iquanwai.confucius.biz.util.XMLHelper;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by justin on 17/7/16.
 */
@XmlRootElement(name = "xml")
@ToString
@NoArgsConstructor
public class NewsMessage {
    @XmlElement(name = "ToUserName")
    private String toUserName;
    @XmlElement(name = "FromUserName")
    private String fromUserName;
    @XmlElement(name = "CreateTime")
    private Long createTime;
    @XmlElement(name = "MsgType")
    private String msgType = "<![CDATA[news]]>";
    @XmlElement(name = "ArticleCount")
    private Integer articleCount;
    @XmlElement(name = "Articles")
    private Articles articles = new Articles();

    public NewsMessage(String wxid, String openid, List<GraphicMessage> graphicMessages) {
        this.createTime = System.currentTimeMillis() / 1000;
        this.fromUserName = XMLHelper.appendCDATA(wxid);
        this.toUserName = XMLHelper.appendCDATA(openid);
        graphicMessages.forEach(articles::add);
        this.articleCount = articles.count();
    }

    @ToString
    @NoArgsConstructor
    public static class Articles{
        @XmlElement(name = "item")
        private List<Article> articleList = Lists.newArrayList();

        public void add(GraphicMessage graphicMessage){
            articleList.add(new Article(graphicMessage));
        }

        public int count(){
            return articleList.size();
        }
    }

    @ToString
    @NoArgsConstructor
    public static class Article {
        @XmlElement(name="Title")
        public String title;
        @XmlElement(name="Description")
        public String description;
        @XmlElement(name="PicUrl")
        public String picUrl;
        @XmlElement(name="Url")
        public String url;

        public Article(GraphicMessage graphicMessage){
            this.title = XMLHelper.appendCDATA(graphicMessage.getTitle());
            this.description = XMLHelper.appendCDATA(graphicMessage.getDescription());
            this.picUrl = XMLHelper.appendCDATA(graphicMessage.getPicUrl());
            this.url = XMLHelper.appendCDATA(graphicMessage.getUrl());
        }
    }
}
