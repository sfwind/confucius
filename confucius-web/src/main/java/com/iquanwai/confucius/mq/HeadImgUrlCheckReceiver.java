package com.iquanwai.confucius.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class HeadImgUrlCheckReceiver {

    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private AccountService accountService;

    private static final String TOPIC = "profile_headImgUrl_check";
    private static final String QUEUE = "profile_headImgUrl_queue";

    private static OkHttpClient client;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        client = new OkHttpClient();

        rabbitMQFactory.initReceiver(QUEUE, TOPIC, message -> {
            logger.info("receive message {}", message);
            JSONObject json = JSON.parseObject(message.getMessage().toString());
            Integer profileId = json.getInteger("profileId");
            String openId = json.getString("openId");
            String preHeadImgUrl = json.getString("headImgUrl");
            checkAndUpdateHeadImgUrl(profileId, openId, preHeadImgUrl);
        });
    }

    private void checkAndUpdateHeadImgUrl(Integer profileId, String openId, String preHeadImgUrl) {
        try {
            Request request = new Request.Builder().url(preHeadImgUrl).get().build();
            Response response = client.newCall(request).execute();
            if (response != null) {
                String errorNo = response.header("X-ErrNo");
                if (errorNo != null) {
                    String realHeadImgUrl = accountService.getRealHeadImgUrlFromWeixin(openId);
                    if (realHeadImgUrl != null) {
                        accountService.updateHeadImageUrl(profileId, realHeadImgUrl);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
