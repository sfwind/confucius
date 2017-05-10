package com.iquanwai.confucius.biz.domain.weixin.message;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.util.CommonUtils;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 17/5/10.
 */
@Data
public class TextMessage {
    private List<String> touser = Lists.newArrayList();
    private String msgtype="text";
    private Text text = new Text();
    private String clientmsgid = CommonUtils.randomString(16);

    public void setContent(String content){
        text.setContent(content);
    }

    public void addUser(String user){
        touser.add(user);
    }

    @Data
    class Text{
        String content;
    }
}
