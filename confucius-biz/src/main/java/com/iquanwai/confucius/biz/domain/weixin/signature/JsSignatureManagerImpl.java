package com.iquanwai.confucius.biz.domain.weixin.signature;

import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import com.iquanwai.confucius.biz.util.SHA1Helper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by yangyuchen on 15-1-30.
 */
@Service
public class JsSignatureManagerImpl implements JsSignatureManager {
    @Autowired
    RestfulHelper restfulHelper;

//    private static String jsapi_ticket;

    private static Map jsApiMap = new HashMap();

    private static Object LOCK = new Object();

    private Logger logger = LoggerFactory.getLogger(JsSignatureManagerImpl.class);

    public JsSignature getJsSignature(int agentId, String url, boolean refresh) {
        String agentIdStr = ""+agentId;

        //判断是否超过间隔
        int interval = ConfigUtils.getJsSignatureInterval();
        Long now = new Date().getTime();
        if((now - (jsApiMap.get(agentIdStr+"_refreshTime")==null?new Long(0):(Long)jsApiMap.get(agentIdStr+"_refreshTime"))) > interval*1000) {
            synchronized (LOCK) {
                if((now - (jsApiMap.get(agentIdStr+"_refreshTime")==null?new Long(0):(Long)jsApiMap.get(agentIdStr+"_refreshTime"))) > interval*1000) {
                    // refresh
                    refreshJsApiTicket(agentIdStr);
                }
            }
        }

        String jsapi_ticket = (String)jsApiMap.get(agentIdStr);

        // if empty
        if(StringUtils.isEmpty(jsapi_ticket)){
            synchronized (LOCK) {
                if(StringUtils.isEmpty((String)jsApiMap.get(agentIdStr))) {
                    refreshJsApiTicket(agentIdStr);
                }
            }
            jsapi_ticket = (String)jsApiMap.get(agentIdStr);
        }

        String noncestr = UUID.randomUUID().toString().replace("-","").substring(0, 11);
        String timestamp =  Long.toString(System.currentTimeMillis());
        Map<String, String> vars = new LinkedHashMap<String, String>();
        vars.put("jsapi_ticket", jsapi_ticket);
        vars.put("noncestr", noncestr);
        vars.put("timestamp", timestamp);
        vars.put("url", url);
        String varStr = getUrlParamsByMap(vars);
//        System.out.println(varStr);
        String signature = SHA1Helper.getSHA1String(varStr);
//        System.out.println(signature);
        JsSignature jsSignature = new JsSignature(ConfigUtils.getAppid(), timestamp, noncestr, signature);
        return jsSignature;
    }

    private void refreshJsApiTicket(String agentId) {
        String jsapi_ticket = restfulHelper.get(ConfigUtils.getJsTicketUrl());

        jsApiMap.put(agentId, jsapi_ticket);
        jsApiMap.put(agentId+"_refreshTime", new Date().getTime());
        logger.info("refresh ticket {}", jsApiMap);
    }

    private static String getUrlParamsByMap(Map<String, String> map) {
        if (map == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey() + "=" + entry.getValue());
            sb.append("&");
        }
        String s = sb.toString();
        if (s.endsWith("&")) {
            s = StringUtils.substringBeforeLast(s, "&");
        }
        return s;
    }
}
