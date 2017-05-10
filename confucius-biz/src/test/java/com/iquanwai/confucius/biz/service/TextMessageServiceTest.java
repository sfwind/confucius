package com.iquanwai.confucius.biz.service;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.domain.weixin.message.TextMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.TextMessageService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 17/5/10.
 */
public class TextMessageServiceTest extends TestBase {
    @Autowired
    private TextMessageService textMessageService;

    @Test
    public void test(){
        TextMessage textMessage = new TextMessage();
        textMessage.setContent("测试\n换行");
        textMessage.addUser("o5h6ywlXxHLmoGrLzH9Nt7uyoHbM");
        textMessage.addUser("o5h6ywsVggC1lVYOvp4-gAdpJ8QI");
        textMessageService.sendMessage(textMessage);
    }
}
