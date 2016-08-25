package com.iquanwai.confucius.web.weixin;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.iquanwai.confucius.biz.domain.weixin.signature.JsSignature;
import com.iquanwai.confucius.biz.domain.weixin.signature.JsSignatureManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Created by yangyuchen on 8/14/14.
 */
public class JsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsController.class);

    @Autowired
    private JsSignatureManager jsSignatureManager;

    private int agentId;
    private String url;
    private String refresh;

    @RequestMapping("/js/signature")
    public String signature(HttpServletResponse response) throws IOException {
        Long startTime = new Date().getTime();
        LOGGER.info("start to get js config info, start time is  "+ startTime);
        JsSignature jsSignature = null;
//        if(refresh!=null && refresh.equals("true")){
//            jsSignature = jsSignatureManager.getJsSignature(agentId, url, true);
//        }else{
//            jsSignature = jsSignatureManager.getJsSignature(agentId, url, false);
//        }
        jsSignature = jsSignatureManager.getJsSignature(agentId, url, false);
        final JsonFactory jsonFactory = new JsonFactory();
        final JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(response.getWriter());
        response.setContentType("application/json");
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("appId", jsSignature.getAppId());
        jsonGenerator.writeStringField("nonceStr", jsSignature.getNonceStr());
        jsonGenerator.writeStringField("signature", jsSignature.getSignature());
        jsonGenerator.writeStringField("timestamp", jsSignature.getTimestamp());
//        jsonGenerator.writeStringField("loginId", String.valueOf(oAuthManager.getLoginIdByUserId(userId())));
        jsonGenerator.writeEndObject();
        jsonGenerator.close();
        response.flushBuffer();
        LOGGER.info("get js config info end, use time is "+ (new Date().getTime()-startTime));
        return null;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }
}
