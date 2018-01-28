package com.iquanwai.confucius.biz.domain.weixin.api;

import java.util.Map;

/**
 * Created by 三十文
 */
public interface WeiXinApiService {
    String getAppAccessToken();

    String generateRedirectOAuthUrl(String state, String codeCallbackUrl);

    WeiXinResult.UserAccessTokenObject exchangeUserAccessTokenByCode(String code);

    Map<String, String> generateJsOAuthParam(String state, String codeCallbackUrl);

    WeiXinResult.MiniUserAccessTokenObject exchangeWeMiniUserAccessTokenByCode(String jsCode);

    WeiXinResult.UserInfoObject getWeiXinUserInfo(String openId, String accessToken);
}
