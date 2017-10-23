package com.iquanwai.confucius.web;

import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQPublisher;
import com.iquanwai.confucius.mq.CacheReloadReceiver;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Created by justin on 16/10/7.
 */
@Controller
@RequestMapping("/cache")
public class CacheReloadController {
    private RabbitMQPublisher rabbitMQPublisher;

    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init(){
        rabbitMQPublisher = rabbitMQFactory.initFanoutPublisher(CacheReloadReceiver.TOPIC);
    }

    @RequestMapping("/class/reload")
    public ResponseEntity<Map<String, Object>> classReload() {
        try {
            rabbitMQPublisher.publish("class");
            return WebUtils.success();
        }catch (Exception e){
            LOGGER.error("reload class", e);
        }
        return WebUtils.error("reload class");
    }

    @RequestMapping("/permission/reload")
    public ResponseEntity<Map<String,Object>> reloadPermission(){
        try {
            rabbitMQPublisher.publish("permission");
            return WebUtils.success();
        } catch (Exception e){
            LOGGER.error("reload permission error",e);
        }
        return WebUtils.error("reload permission");
    }

    @RequestMapping("/region/reload")
    public ResponseEntity<Map<String,Object>> reloadRegion(){
        try{
            rabbitMQPublisher.publish("region");
            return WebUtils.success();
        } catch (Exception e){
            return WebUtils.error("reload region");
        }
    }

    @RequestMapping("/rise/member/reload")
    public ResponseEntity<Map<String,Object>> riseMemberReload(){
        try{
            rabbitMQPublisher.publish("rise_member");
            return WebUtils.success();
        } catch (Exception e){
            return WebUtils.error("reload riseMember error");
        }
    }

    @RequestMapping("/weixin/message/reload")
    public ResponseEntity<Map<String,Object>> weixinMessageReload(){
        try{
            rabbitMQPublisher.publish("weixin_message");
            return WebUtils.success();
        } catch (Exception e){
            return WebUtils.error("reload riseMember error");
        }
    }


}
