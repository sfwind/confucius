package com.iquanwai.confucius.biz.util;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by justin on 16/8/7.
 */
public class CommonUtils {
    public static String urlReplace(String url, Map<String, String> replacer){
        if(StringUtils.isNotEmpty(url) && replacer!=null) {
            for (Iterator<Map.Entry<String, String>> it =
                 replacer.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, String> entry = it.next();
                url = StringUtils.replace(url, "{"+entry.getKey()+"}", entry.getValue());
            }
        }
        return url;
    }

    public static Map<String, Object> jsonToMap(String json){
        if(StringUtils.isEmpty(json)){
            return Maps.newHashMap();
        }
        Map<String, Object> gsonMap = new Gson().fromJson(json,
                new TypeToken<Map<String, Object>>() {
                }.getType());
        return gsonMap;
    }

    public static String mapToJson(Map<String, Object> map){
        if(MapUtils.isEmpty(map)){
            return "";
        }
        String json = new Gson().toJson(map,
                new TypeToken<Map<String, Object>>() {
                }.getType());
        return json;
    }

    public static boolean isError(String json){
        if(StringUtils.isEmpty(json)){
            return false;
        }
        Map<String, Object> gsonMap = jsonToMap(json);
        if(gsonMap.get("errcode")!=null && gsonMap.get("errmsg")!=null
            && !gsonMap.get("errcode").equals(0.0) && !gsonMap.get("errcode").equals("0")){
            return true;
        }
        return false;
    }

    public static String appendAccessToken(String url, String accessToken){
        if(url.contains("?")){
            return url+"&accessToken="+accessToken;
        }else{
            return url+"?accessToken="+accessToken;
        }
    }

    public static String getUrlParamsByMap(Map<String, String> map) {
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
