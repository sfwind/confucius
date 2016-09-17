package com.iquanwai.confucius.biz.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

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

    public static String getUrlParamsByMap(final Map<String, String> map) {
        if (map == null) {
            return "";
        }
        List<String> list = new ArrayList(map.keySet());
        Collections.sort(list);

        List<String> kvList = Lists.transform(list, new Function<String, String>() {
            public String apply(String input) {
                return input+"="+map.get(input);
            }
        });

        return StringUtils.join(kvList.iterator(), "&");
    }

    public static String randomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static String sign(final Map<String, String> params){
        List<String> list = new ArrayList(params.keySet());
        Collections.sort(list);

        List<String> kvList = Lists.transform(list, new Function<String, String>() {
            public String apply(String input) {
                return input+"="+params.get(input);
            }
        });

        String digest = StringUtils.join(kvList.iterator(), "&")
                .concat("&key=")
                .concat(ConfigUtils.getAPIKey());

        return MessageDigestHelper.getMD5String(digest);
    }

}
