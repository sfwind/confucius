package com.iquanwai.confucius.biz.domain.weixin.api;

import com.iquanwai.confucius.biz.po.common.customer.Profile;

import java.util.Map;

/**
 * Created by 三十文
 */
public interface WeiXinApiService {
    String getAppAccessToken();

    String generateRedirectOAuthUrl(String state, String codeCallbackUrl);

    WeiXinResult.UserAccessTokenObject exchangeUserAccessTokenByCode(String code, Profile.ProfileType profileType);

    Map<String, String> generateJsOAuthParam(String state, String codeCallbackUrl);

    WeiXinResult.MiniUserAccessTokenObject exchangeMiniUserAccessTokenByCode(String jsCode);

    WeiXinResult.UserInfoObject getWeiXinUserInfo(String openId, String accessToken);

    WeiXinResult.RefreshTokenObject refreshWeiXinAccessToken(String accessToken);
}
