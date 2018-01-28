package com.iquanwai.confucius.biz.domain.weixin.api;

import lombok.Data;

/**
 * Created by 三十文
 */
public class WeiXinResult {

    /**
     * 通过 code 换取网页授权 access_token (用户级)
     */
    @Data
    public static class UserAccessTokenObject {
        private String openId;
        private String accessToken;
        private String refreshToken;
    }

    /**
     * 通过用户 accessToken 和 code 换取用户信息
     */
    @Data
    public static class UserInfoObject {
        private String openId;
        private String nickName;
        private String headImgUrl;
        private String sex;
        private String country;
        private String province;
        private String city;
        private String unionId;
    }

}
