package com.iquanwai.confucius.biz.service;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Created by justin on 16/10/12.
 */
public class TemplateMessageServiceTest extends TestBase {
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private CourseProgressService progressService;

    @Test
    public void testSend(){

        String[] arrs = {
                "o5h6ywsiXYMcLlex2xt7DRAgQX-A",
        };

        for(String openid:arrs) {
            try {
                TemplateMessage templateMessage = new TemplateMessage();
                templateMessage.setTouser(openid);

                templateMessage.setTemplate_id(ConfigUtils.willCloseMsgKey());
                Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                templateMessage.setData(data);
                data.put("first", new TemplateMessage.Keyword("RISE试用期即将结束。"));
                data.put("keyword1", new TemplateMessage.Keyword("RISE"));
                data.put("keyword2", new TemplateMessage.Keyword("2017-02-28"));
                data.put("remark", new TemplateMessage.Keyword("试用期截止日当天积分前100名的用户，RISE继续开放，其余用户到期后关闭。当前排名详见管理员通知。"));
                templateMessageService.sendMessage(templateMessage);
            }catch (Exception e){
                System.out.println(e);
            }
        }
    }

    @Test
    public void closeTest(){
        progressService.noticeWillCloseMember();
    }
}
