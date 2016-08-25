package com.iquanwai.confucius.biz.entity.menu;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by justin on 14-8-6.
 */
public class ViewMenuDto extends MenuDto {
    /** 跳转url，用户点击菜单后跳转的url */
    private String url;
    private String type = "view";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    @Override
    protected void injectMap(Map map){
        if(map==null){
            map = new HashMap();
        }
        map.put("url", url);
        map.put("type", type);
    }
}
