package com.iquanwai.confucius.biz.domain.weixin.oauth;

import com.iquanwai.confucius.biz.domain.weixin.api.WeiXinResult;
import com.iquanwai.confucius.biz.po.Callback;

import java.io.IOException;

/**
 * Created by justin on 14-7-28.
 */
public interface OAuthService {

    String WE_CHAT_STATE_COOKIE_NAME = "_act";
    String PC_STATE_COOKIE_NAME = "_qt";

    /**
     * 在访问用户授权页面之前，预生成 Callback 对象，存储 state 已经对应的回调 url
     */
    Callback initCallback(String callbackUrl, String state, String checkParam);

    boolean checkCallbackAuthority(String state, String checkParam);

    Callback supplementMobileCallback(String state, WeiXinResult.UserAccessTokenObject userAccessTokenObject);

    Callback supplementPcCallback(String state, WeiXinResult.UserAccessTokenObject userAccessTokenObject);

    Callback initMiniCallback(String state, WeiXinResult.MiniUserAccessTokenObject miniUserAccessTokenObject);

    Callback supplementCallbackUnionId(String state, String unionId);

    Callback getCallbackByState(String state);

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

}
