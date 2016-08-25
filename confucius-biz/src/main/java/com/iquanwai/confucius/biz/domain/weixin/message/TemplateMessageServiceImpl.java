package com.iquanwai.confucius.biz.domain.weixin.message;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by justin on 16/8/10.
 */
@Service
public class TemplateMessageServiceImpl implements TemplateMessageService {
    @Autowired
    private RestfulHelper restfulHelper;

    public boolean sendMessage(TemplateMessage templateMessage) {
        String url = SEND_MESSAGE_URL;
        String json = new Gson().toJson(templateMessage);
        String body = restfulHelper.post(url, json);
        // TODO 记录messageid
        return !CommonUtils.isError(body);
    }

    public String getTemplateId(String templateShortId) {
        Map<String, String> map = Maps.newHashMap();
        map.put("template_id_short", templateShortId);

        String url = SEND_MESSAGE_URL;
        String json = new Gson().toJson(map);
        String body = restfulHelper.post(url, json);
        if(CommonUtils.isError(body)){
            return "";
        }
        Map<String, Object> response = CommonUtils.jsonToMap(body);
        return (String)response.get("template_id");
    }
}
