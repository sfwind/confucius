package com.iquanwai.confucius.biz.domain.weixin.message.customer;

import com.google.gson.Gson;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by justin on 17/7/8.
 */
@Service
public class CustomerMessageServiceImpl implements CustomerMessageService {
    @Autowired
    private RestfulHelper restfulHelper;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void sendCustomerMessage(String openid, String message, Integer type) {
        if (TEXT.equals(type)) {
            TextCustomerMessage customerMessage = new TextCustomerMessage(openid, message);
            Gson gson = new Gson();
            String json = gson.toJson(customerMessage);
            restfulHelper.post(SEND_CUSTOMER_MESSAGE_URL, json);
        } else if(IMAGE.equals(type)){
            ImageCustomerMessage customerMessage = new ImageCustomerMessage(openid, message);
            Gson gson = new Gson();
            String json = gson.toJson(customerMessage);
            restfulHelper.post(SEND_CUSTOMER_MESSAGE_URL, json);
        } else if(VOICE.equals(type)){
            VoiceCustomerMessage customerMessage = new VoiceCustomerMessage(openid, message);
            Gson gson = new Gson();
            String json = gson.toJson(customerMessage);
            restfulHelper.post(SEND_CUSTOMER_MESSAGE_URL, json);
        }
    }
}
