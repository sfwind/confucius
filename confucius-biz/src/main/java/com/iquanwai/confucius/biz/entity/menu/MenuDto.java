package com.iquanwai.confucius.biz.entity.menu;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by justin on 14-8-6.
 */
public class MenuDto implements Serializable {
    /** 菜单名 */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public final void fillParameter(Map map){
        map.put("name", name);
        injectMap(map);
    }

    protected void injectMap(Map map){
        // override by child class
    }
}
