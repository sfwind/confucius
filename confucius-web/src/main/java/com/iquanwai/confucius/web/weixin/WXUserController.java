package com.iquanwai.confucius.web.weixin;

import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.util.ThreadPool;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQPublisher;
import com.iquanwai.confucius.mq.CacheReloadReceiver;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.util.Map;

@RestController
@RequestMapping("/wx/user")
public class WXUserController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    private RabbitMQPublisher rabbitMQPublisher;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init(){
        rabbitMQPublisher = rabbitMQFactory.initFanoutPublisher(CacheReloadReceiver.RISE_TOPIC);
    }


    @RequestMapping(value = "/update/image",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> updateWXImg(@RequestParam("riseId")String riseId){
        logger.info("更新{}头像",riseId);
        ThreadPool.execute(()->{
            Profile profile = accountService.getProfileByRiseId(riseId);
            if(profile!=null){
                //更新用户头像
                accountService.updateProfileByWeiXin(profile);
                try {
                    //发送mq
                    rabbitMQPublisher.publish("school_friend");
                } catch (ConnectException e) {
                    logger.error(e.getLocalizedMessage(),e);
                }
            }
        });
        return WebUtils.success();
    }


}
