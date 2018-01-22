package com.iquanwai.confucius.biz.domain.weixin.oauth;

import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Map;

/**
 * Created by justin on 14-7-28.
 */
public interface OAuthService {
    String OAUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid={appid}&redirect_uri={redirect_url}&response_type=code&scope=snsapi_base&state={state}#wechat_redirect";
    String OAUTH_ASK_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid={appid}&redirect_uri={redirect_url}&response_type=code&scope=snsapi_userinfo&state={state}#wechat_redirect";
    String REFRESH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid={appid}&grant_type=refresh_token&refresh_token={refresh_token}";
    String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid={appid}&secret={secret}&code={code}&grant_type=authorization_code";
    String WE_MINI_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/jscode2session?appid={APPID}&secret={SECRET}&js_code={JSCODE}&grant_type=authorization_code";

    String RISE_PC_OAUTH_URL = ConfigUtils.domainName() + "/wx/oauth/pc/code";

    String WE_CHAT_STATE_COOKIE_NAME = "_act";
    String PC_STATE_COOKIE_NAME = "_qt";
    String ACCESS_ASK_TOKEN_COOKIE_NAME = "_ask";

    int SEVEN_DAYS = 60 * 60 * 24 * 7;

    /**
     * 组装微信授权页的url，记录回调url
     */
    String redirectUrl(String callbackUrl, String authUrl);

    /**
     * 新增pc获取accessToken
     */
    Callback pcAccessToken(String code, String state);

    /**
     * 根据code，获取accessToken，返回Callcack
     */
    Callback accessToken(String code, String state);

    /**
     * 根据 state，获取授权用户的openid
     */
    String openId(String state);

    /**
     * 根据 state，查询授权用户的 openId
     */
    String pcOpenId(String state);

    /**
     * 刷新accessToken
     */
    String refresh(String accessToken);

    /**
     * 根据微信小程序返回的 code 参数，换取 AccessToken 并且保存在数据库中
     * @param code 小程序前端请求返回的 code
     */
    Callback weMiniAccessToken(String code) throws IOException;

    /**
     * 根据请求中的回调 URI，拼凑出用于生成微信二维码的参数
     */
    Map<String, String> pcRedirectUrl(String callbackUrl);

    Pair<Integer, Callback> initOpenId(Callback callback);
}
