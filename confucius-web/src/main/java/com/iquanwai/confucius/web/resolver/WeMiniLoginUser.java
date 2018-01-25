package com.iquanwai.confucius.web.resolver;

import com.iquanwai.confucius.biz.util.ConfigUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 三十文
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeMiniLoginUser {

    private Integer id;
    private String openId;
    private String weMiniOpenId;
    private String unionId;

    public static WeMiniLoginUser defaultUser() {
        return new WeMiniLoginUser(
                ConfigUtils.getDefaultProfileId(),
                ConfigUtils.getDefaultOpenid(),
                ConfigUtils.getDefaultWeMiniOpenId(),
                ConfigUtils.getDefaultUnionId());
    }

}
