package com.iquanwai.confucius.biz.domain.weixin.api;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by 三十文
 * 此接口用来放置所有微信调用相关接口
 */
@Service
public class WeiXinApiServiceImpl implements WeiXinApiService {

    @Autowired
    private RestfulHelper restfulHelper;

    /** 获取应用级 accessToken url */
    String APP_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}";
    /** 引导用户授权，在回调接口中返回 code 值 */
    String OAUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid={appid}&redirect_uri={redirect_url}&response_type=code&scope=snsapi_userinfo&state={state}#wechat_redirect";
    /** 获取当前用户的 accessToken */
    String USER_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid={appid}&secret={secret}&code={code}&grant_type=authorization_code";
    /** 当前用户 accessToken 过期时，通过 refreshToken 获取新 accessToken 和 refreshToken */
    String REFRESH_USER_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid={appid}&grant_type=refresh_token&refresh_token={refresh_token}";
    /** 通过 jscode 换取 accessToken，运用在小程序 */
    String WE_MINI_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={jscode}&grant_type=authorization_code";
    /** 获取用户信息，accessToken 为用户 accessToken，openId 为当前平台对应 openId，包含昵称、头像、unionId等信息 */
    String SNS_API_USER_INFO = "https://api.weixin.qq.com/sns/userinfo?access_token={access_token}&openid={openid}&lang=zh_CN";
    /** 获取用户基本信息，accessToken 为应用级调用接口凭证，包含昵称、头像、unionId、省份、城市、性别等信息 */
    String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token={access_token}&openid={openid}&lang=zh_CN";
    /** 获取用户列表，单次最大上线10000，从第一个开始拉取 */
    String GET_USERS_URL = "https://api.weixin.qq.com/cgi-bin/user/get?access_token={access_token}";
    /** 获取用户列表，单次最大上限10000，从 next_openid 开始拉取 */
    String GET_NEXT_USERS_URL = "https://api.weixin.qq.com/cgi-bin/user/get?access_token={access_token}&next_openid={next_openid}";
    /** 获取黑名单人员 */
    String LIST_BLACKLIST_URL = "https://api.weixin.qq.com/cgi-bin/tags/members/getblacklist?access_token={access_token}";
    /** 将人员标记为黑名单 */
    String BATCH_BALCKLIST_URL = "https://api.weixin.qq.com/cgi-bin/tags/members/batchblacklist?access_token={access_token}";
    /** 将人员移除黑名单 */
    String UNBATCH_BACKLIST_URL = "https://api.weixin.qq.com/cgi-bin/tags/members/batchunblacklist?access_token={access_token}";

    private static final String IP_REGEX = "(\\d*\\.){3}\\d*";
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @return 应用级别的 accessToken
     */
    @Override
    public String getAppAccessToken() {
        Map<String, String> params = Maps.newHashMap();
        params.put("appid", ConfigUtils.getAppid());
        params.put("secret", ConfigUtils.getSecret());
        String requestUrl = CommonUtils.placeholderReplace(APP_ACCESS_TOKEN_URL, params);
        String body = restfulHelper.get(requestUrl);
        Map<String, Object> accessTokenObject = CommonUtils.jsonToMap(body);
        String accessToken = accessTokenObject.get("access_token").toString();
        logger.info("最新请求 accessToken 为：{}", accessToken);
        return accessToken;
    }

    /**
     * 手机访问，组装授权请求
     * @param state 请求授权链接随机数
     * @return 组装之后的 url
     */
    @Override
    public String generateRedirectOAuthUrl(String state, String codeCallbackUrl) {
        Map<String, String> params = Maps.newHashMap();
        params.put("appid", ConfigUtils.getAppid());
        params.put("scope", "snsapi_userinfo");
        try {
            params.put("redirect_url", URLEncoder.encode(ConfigUtils.adapterDomainName() + codeCallbackUrl, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        params.put("state", state);
        return CommonUtils.placeholderReplace(OAUTH_URL, params);
    }

    /**
     * 拼接 web 页面请求的 js 对象
     * @param state 请求授权链接随机数
     * @param codeCallbackUrl 用户授权成功之后，微信回调的接口
     * @return 返回前端的请求参数
     */
    @Override
    public Map<String, String> generateJsOAuthParam(String state, String codeCallbackUrl) {
        Map<String, String> params = Maps.newHashMap();
        params.put("appid", ConfigUtils.getRisePcAppid());
        params.put("scope", "snsapi_userinfo");
        try {
            params.put("redirect_uri", URLEncoder.encode(ConfigUtils.adapterDomainName() + codeCallbackUrl, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        params.put("state", state);
        params.put("style", "");
        params.put("href", "");
        return params;
    }

    /**
     * 用户同意授权，微信回调返回 code，根据 code 换取 accessToken
     * @param code 微信返回 code
     */
    @Override
    public WeiXinResult.UserAccessTokenObject exchangeUserAccessTokenByCode(String code) {
        Map<String, String> params = Maps.newHashMap();
        params.put("appid", ConfigUtils.getAppid());
        params.put("secret", ConfigUtils.getSecret());
        params.put("code", code);
        String requestUrl = CommonUtils.placeholderReplace(USER_ACCESS_TOKEN_URL, params);
        String body = restfulHelper.get(requestUrl);

        WeiXinResult.UserAccessTokenObject userAccessTokenObject = new WeiXinResult.UserAccessTokenObject();
        try {
            Map<String, Object> result = CommonUtils.jsonToMap(body);
            String accessToken = result.get("access_token").toString();
            String openId = result.get("openid").toString();
            String refreshToken = result.get("refresh_token").toString();

            userAccessTokenObject.setAccessToken(accessToken);
            userAccessTokenObject.setRefreshToken(refreshToken);
            userAccessTokenObject.setOpenId(openId);
            return userAccessTokenObject;
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * 获取小程序根据 code 交换回来的 accessToken 对象
     * @param jsCode 小程序授权之后返回的 jsCode
     */
    @Override
    public WeiXinResult.MiniUserAccessTokenObject exchangeWeMiniUserAccessTokenByCode(String jsCode) {
        Map<String, String> params = Maps.newHashMap();
        params.put("appid", ConfigUtils.getAppid());
        params.put("secret", ConfigUtils.getSecret());
        params.put("jscode", jsCode);
        String requestUrl = CommonUtils.placeholderReplace(WE_MINI_ACCESS_TOKEN_URL, params);
        String body = restfulHelper.get(requestUrl);

        WeiXinResult.MiniUserAccessTokenObject miniUserAccessTokenObject = new WeiXinResult.MiniUserAccessTokenObject();
        try {
            Map<String, Object> result = CommonUtils.jsonToMap(body);
            String openId = result.get("openid").toString();
            String accessToken = result.get("session_key").toString();
            String unionId = result.get("unionid").toString();

            miniUserAccessTokenObject.setOpenId(openId);
            miniUserAccessTokenObject.setAccessToken(accessToken);
            miniUserAccessTokenObject.setUnionId(unionId);
            return miniUserAccessTokenObject;
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * 获取微信上用户信息
     * @param openId 各个平台用户对应的 openId
     * @param accessToken 针对各个平台的用户 accessToken
     * @return 返回用户信息对象
     */
    @Override
    public WeiXinResult.UserInfoObject getWeiXinUserInfo(String openId, String accessToken) {
        Map<String, String> params = Maps.newHashMap();
        params.put("openid", openId);
        params.put("access_token", accessToken);
        String requestUrl = CommonUtils.placeholderReplace(USER_INFO_URL, params);
        String body = restfulHelper.get(requestUrl);

        WeiXinResult.UserInfoObject userInfoObject = new WeiXinResult.UserInfoObject();
        try {
            Map<String, Object> result = CommonUtils.jsonToMap(body);
            String newOpenId = result.get("openid").toString();
            String nickName = result.get("nickname").toString();
            String headImgUrl = result.get("headimgurl").toString();
            String country = result.get("country").toString();
            String province = result.get("province").toString();
            String city = result.get("city").toString();
            String unionId = result.get("unionId").toString();
            userInfoObject.setOpenId(newOpenId);
            userInfoObject.setNickName(nickName);
            userInfoObject.setHeadImgUrl(headImgUrl);
            userInfoObject.setCountry(country);
            userInfoObject.setProvince(province);
            userInfoObject.setCity(city);
            userInfoObject.setUnionId(unionId);
            return userInfoObject;
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

}
