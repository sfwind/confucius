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
    private String nickName;
    private String headImgUrl;

    public static WeMiniLoginUser defaultUser() {
        return new WeMiniLoginUser(
                ConfigUtils.getDefaultProfileId(),
                ConfigUtils.getDefaultOpenid(),
                "三十文",
                "http://wx.qlogo.cn/mmopen/Q3auHgzwzM7wkhob9zgicD3IJxG1tLVSSe9qdzR1qUGXz6BwPv73sr67iaTEibcA1sNic3Roib4DgXCVG4IWe0zPAKJnlo5r4NibezssS6naic6dkM/0");
    }

}
