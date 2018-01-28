package com.iquanwai.confucius.biz.domain.weixin.oauth;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.wx.CallbackDao;
import com.iquanwai.confucius.biz.domain.weixin.api.WeiXinResult;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OAuthServiceImpl implements OAuthService {

    @Autowired
    private CallbackDao callbackDao;
    @Autowired
    private RestfulHelper restfulHelper;


    private static final String IP_REGULAR = "(\\d*\\.){3}\\d*";
    private Logger logger = LoggerFactory.getLogger(getClass());

    /** 获取当前用户的 accessToken */
    String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid={appid}&secret={secret}&code={code}&grant_type=authorization_code";
    /** 当前用户 accessToken 过期时，通过 refreshToken 获取新 accessToken 和 refreshToken */
    String REFRESH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid={appid}&grant_type=refresh_token&refresh_token={refresh_token}";
    /** 通过 jscode 换取 accessToken，运用在小程序 */
    String WE_MINI_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={jscode}&grant_type=authorization_code";

    @Override
    /**
     * 在访问用户授权页面之前，预生成 Callback 对象，存储 state 已经对应的回调 url
     */
    public Callback initCallback(String callbackUrl, String state) {
        String ip = getIpFromUrl(callbackUrl);
        if (ip != null) {
            callbackUrl = callbackUrl.replace("http://" + ip, ConfigUtils.domainName());
        }

        // 为了存储 state 值，不得不在此声明 callback
        Callback callback = new Callback();
        callback.setState(state);
        callback.setCallbackUrl(callbackUrl);
        callbackDao.insert(callback);
        return callback;
    }

    /**
     * 使用 code 换取 mobile 用户凭证之后，将凭证存储数据库
     */
    @Override
    public Callback supplementMobileCallback(String state, WeiXinResult.UserAccessTokenObject userAccessTokenObject) {
        Callback callback = callbackDao.queryByState(state);
        if (callback == null) {
            logger.info("code 回调中的 state，Callback 中不存在，state: {}", state);
            return null;
        } else {
            callback.setAccessToken(userAccessTokenObject.getAccessToken());
            callback.setRefreshToken(userAccessTokenObject.getRefreshToken());
            callback.setOpenid(userAccessTokenObject.getOpenId());
            callbackDao.updateFields(callback);
            return callback;
        }
    }

    /**
     * 使用 code 换取 pc 用户凭证之后，将凭证存储数据库
     */
    @Override
    public Callback supplementPcCallback(String state, WeiXinResult.UserAccessTokenObject userAccessTokenObject) {
        Callback callback = callbackDao.queryByState(state);
        if (callback == null) {
            logger.info("code 回调中的 state，Callback 中不存在，state: {}", state);
            return null;
        } else {
            callback.setPcAccessToken(userAccessTokenObject.getAccessToken());
            callback.setPcOpenid(userAccessTokenObject.getOpenId());
            callbackDao.updateFields(callback);
            return callback;
        }
    }

    public Callback supplementMiniCallback(String state, WeiXinResult.UserAccessTokenObject userAccessTokenObject) {

    }

    /**
     * 获取用户信息之后，将用户信息的 unionId 完善到 callback 中
     * @param state 随机数
     * @param unionId 公众平台 unionId
     */
    @Override
    public Callback supplementCallbackUnionId(String state, String unionId) {
        Callback callback = callbackDao.queryByState(state);
        callback.setUnionId(unionId);
        callbackDao.updateFields(callback);
        return callback;
    }

    @Override
    public Callback getCallbackByState(String state) {
        return callbackDao.queryByState(state);
    }

    @Override
    public String openId(String state) {
        if (state == null) {
            return null;
        }
        Callback callback = callbackDao.queryByState(state);
        if (callback == null) {
            logger.error("accessToken {} is invalid", state);
            return null;
        }
        return callback.getOpenid();
    }

    @Override
    public String pcOpenId(String act) {
        if (act == null) {
            logger.info("error，pc _qt is null");
            return null;
        }
        Callback callback = callbackDao.queryByState(act);
        if (callback == null) {
            logger.error("pcAccessToken {} is invalid", act);
            return null;
        }
        return callback.getOpenid();
    }

    @Override
    public String refresh(String accessToken) {
        Callback callback = callbackDao.queryByAccessToken(accessToken);

        if (callback == null) {
            logger.error("accessToken {} is invalid", accessToken);
            return null;
        }

        String requestUrl = REFRESH_TOKEN_URL;

        Map<String, String> params = Maps.newHashMap();
        params.put("appid", ConfigUtils.getAppid());
        params.put("refresh_token", callback.getRefreshToken());
        requestUrl = CommonUtils.placeholderReplace(requestUrl, params);
        String body = restfulHelper.get(requestUrl);
        Map<String, Object> result = CommonUtils.jsonToMap(body);
        String newAccessToken = (String) result.get("access_token");

        // 刷新accessToken
        callbackDao.refreshToken(callback.getState(), newAccessToken);
        return newAccessToken;
    }

    @Override
    public Callback weMiniAccessToken(String code) throws IOException {
        String requestUrl = WE_MINI_ACCESS_TOKEN_URL.replace("{APPID}", ConfigUtils.getWeMiniAppId())
                .replace("{SECRET}", ConfigUtils.getWeMiniAppSecret())
                .replace("{JSCODE}", code);
        ResponseBody responseBody = restfulHelper.getPlain(requestUrl);
        JSONObject resultJson = JSONObject.parseObject(responseBody.string());
        logger.info(resultJson.toString());
        String sessionKey = resultJson.getString("session_key");
        String openId = resultJson.getString("openid");
        String unionId = resultJson.getString("unionid");
        Callback callback = new Callback();
        String state = CommonUtils.randomString(32);
        callback.setState(state);
        callback.setWeMiniAccessToken(sessionKey);
        callback.setUnionId(unionId);
        callback.setWeMiniOpenid(openId);
        int insertResult = 0;
        insertResult = callbackDao.insert(callback);
        if (insertResult > 0) {
            return callback;
        } else {
            throw new RuntimeException("callback 表插入失败，state：" + state);
        }
    }

    private static String getIpFromUrl(String url) {
        Pattern ipPattern = Pattern.compile(IP_REGULAR);
        Matcher matcher = ipPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

}
