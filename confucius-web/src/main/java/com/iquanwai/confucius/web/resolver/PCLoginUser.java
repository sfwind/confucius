package com.iquanwai.confucius.web.resolver;

import com.iquanwai.confucius.biz.util.ConfigUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PCLoginUser {
    private Integer id;
    private Integer profileId;
    private String openId;
    private Integer role;
    private String signature;

    private LoginUser weixin;

    public static PCLoginUser defaultUser() {
        return new PCLoginUser(ConfigUtils.getDefaultProfileId(), ConfigUtils.getDefaultProfileId(),
                ConfigUtils.getDefaultOpenid(), 5, null, LoginUser.defaultUser());
    }
}
