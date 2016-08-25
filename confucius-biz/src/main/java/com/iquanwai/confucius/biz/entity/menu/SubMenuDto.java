package com.iquanwai.confucius.biz.entity.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 14-8-6.
 */
public class SubMenuDto extends MenuDto {
    private List<MenuDto> menus;

    public SubMenuDto(){
        menus = new ArrayList<MenuDto>();
    }

    public List<MenuDto> getMenus() {
        return menus;
    }

    public void setMenus(List<MenuDto> menus) {
        this.menus = menus;
    }

    public void addMenu(MenuDto menuDto){
        menus.add(menuDto);
    }

    @Override
    protected void injectMap(Map map){
        if(map==null){
            map = new HashMap();
        }
        List<Map> subMenuList = new ArrayList<Map>();
        for(MenuDto menuDto: menus){
            Map subMap = new HashMap();
            menuDto.fillParameter(subMap);
            subMenuList.add(subMap);
        }
        map.put("sub_button", subMenuList);
    }

}
