package com.iquanwai.confucius.biz.domain.weixin.message;

import com.google.gson.Gson;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by justin on 17/5/10.
 */
@Service
public class TextMessageServiceImpl implements TextMessageService {
    @Autowired
    private RestfulHelper restfulHelper;

    @Override
    public boolean sendMessage(TextMessage textMessage) {
        if(ConfigUtils.messageSwitch()) {
            String url = SEND_MESSAGE_URL;
            String json = new Gson().toJson(textMessage);
            String body = restfulHelper.post(url, json);
            return StringUtils.isNotEmpty(body);
        }

        return false;
    }
}
