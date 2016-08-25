package com.iquanwai.confucius.biz.entity.menu;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by justin on 14-8-6.
 */
public class ClickMenuDto extends MenuDto{
    /** 菜单的key，发生click事件可以根据key判断用户使用了哪个菜单 */
    private String key;
    private String type = "click";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    @Override
    protected void injectMap(Map map){
        if(map==null){
            map = new HashMap();
        }
        map.put("key", key);
        map.put("type", type);
    }
}
