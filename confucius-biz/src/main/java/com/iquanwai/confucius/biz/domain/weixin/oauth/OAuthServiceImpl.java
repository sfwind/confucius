package com.iquanwai.confucius.biz.domain.weixin.oauth;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.wx.CallbackDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.exception.NotFollowingException;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by justin on 16/8/13.
 */
@Service
public class OAuthServiceImpl implements OAuthService {

    @Autowired
    private AccountService accountService;
    @Autowired
    private CallbackDao callbackDao;
    @Autowired
    private RestfulHelper restfulHelper;

    private static final String REDIRECT_PATH = "/wx/oauth/code";

    private static final String REDIRECT_ASK_PATH = "/wx/oauth/code";

    private static final String IP_REGULAR = "(\\d*\\.){3}\\d*";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String redirectUrl(String callbackUrl, String authUrl) {
        String requestUrl = authUrl;
        Callback callback = new Callback();
        String ip = getIPFromUrl(callbackUrl);
        if (ip != null) {
            callbackUrl = callbackUrl.replace("http://" + ip, ConfigUtils.domainName());
        }
        callback.setCallbackUrl(callbackUrl);
        String state = CommonUtils.randomString(32);
        callback.setState(state);
        try {
            callbackDao.insert(callback);
        } catch (SQLException e) {
            callback.setState(CommonUtils.randomString(32));
            try {
                callbackDao.insert(callback);
            } catch (SQLException e1) {
                logger.error(e1.getLocalizedMessage(), e1);
            }
        }
        logger.info("state is {}", callback.getState());

        Map<String, String> params = Maps.newHashMap();
        params.put("appid", ConfigUtils.getAppid());
        try {
            params.put("redirect_url", URLEncoder.encode(ConfigUtils.adapterDomainName() + (OAUTH_URL.equals(authUrl) ? REDIRECT_PATH : REDIRECT_ASK_PATH), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        params.put("state", state);
        requestUrl = CommonUtils.placeholderReplace(requestUrl, params);
        return requestUrl;
    }

    @Override
    public String openId(String accessToken) {
        if (accessToken == null) {
            return null;
        }
        Callback callback = callbackDao.queryByAccessToken(accessToken);
        if (callback == null) {
            logger.error("accessToken {} is invalid", accessToken);
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
        Callback callback = callbackDao.queryByPcAccessToken(act);
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
    public Callback accessToken(String code, String state) {
        Callback callback = callbackDao.queryByState(state);
        if (callback == null) {
            logger.error("state {} is not found", state);
            return null;
        }
        String requestUrl = ACCESS_TOKEN_URL;

        Map<String, String> params = Maps.newHashMap();
        params.put("appid", ConfigUtils.getAppid());
        params.put("secret", ConfigUtils.getSecret());
        params.put("code", code);
        requestUrl = CommonUtils.placeholderReplace(requestUrl, params);
        String body = restfulHelper.get(requestUrl);
        Map<String, Object> result = CommonUtils.jsonToMap(body);

        String accessToken = (String) result.get("access_token");
        String openid = (String) result.get("openid");
        String refreshToken = (String) result.get("refresh_token");

        // 更新accessToken，refreshToken，openid
        callback.setOpenid(openid);
        callback.setRefreshToken(refreshToken);
        callback.setAccessToken(accessToken);
        logger.info("update callback, state:{}, accessToken:{}, refreshToken:{}, openId:{}, code:{}", state, accessToken, refreshToken, openid, code);
        callbackDao.updateUserInfo(state, accessToken, refreshToken, openid);

        return callback;
    }

    @Override
    public Callback pcAccessToken(String code, String state) {
        Callback callback = callbackDao.queryByState(state);
        if (callback == null) {
            logger.error("state {} is not found", state);
            return null;
        }
        String requestUrl = ACCESS_TOKEN_URL;

        Map<String, String> params = Maps.newHashMap();
        params.put("appid", ConfigUtils.getRisePcAppid());
        params.put("secret", ConfigUtils.getRisePcSecret());
        params.put("code", code);
        requestUrl = CommonUtils.placeholderReplace(requestUrl, params);
        String body = restfulHelper.get(requestUrl);
        Map<String, Object> result = CommonUtils.jsonToMap(body);

        String accessToken = (String) result.get("access_token");
        String openid = (String) result.get("openid");
        String refreshToken = (String) result.get("refresh_token");
        // 更新accessToken，refreshToken，openid
        logger.info("update callback, state:{},pcAccessToken:{},refreshToken:{},pcOpenId:{},code:{}", state, accessToken, refreshToken, openid, code);
        // pc登录，先将用户的openid存下来
        callback.setPcOpenid(openid);
        callback.setRefreshToken(refreshToken);
        callback.setPcAccessToken(accessToken);
        callbackDao.updatePcUserInfo(state, accessToken, refreshToken, openid);

        return callback;
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
        try {
            insertResult = callbackDao.insert(callback);
        } catch (SQLException e) {
            callback.setState(CommonUtils.randomString(32));
            try {
                insertResult = callbackDao.insert(callback);
            } catch (SQLException e1) {
                logger.error(e1.getLocalizedMessage(), e1);
            }
        }
        if (insertResult > 0) {
            return callback;
        } else {
            throw new RuntimeException("callback 表插入失败，state：" + state);
        }
    }

    @Override
    public Map<String, String> pcRedirectUrl(String callbackUrl) {
        Callback callback = new Callback();
        try {
            callbackUrl = URLDecoder.decode(callbackUrl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        String ip = getIPFromUrl(callbackUrl);
        if (ip != null) {
            callbackUrl = callbackUrl.replace("http://" + ip, ConfigUtils.domainName());
        }
        callback.setCallbackUrl(callbackUrl);
        String state = CommonUtils.randomString(32);
        callback.setState(state);
        logger.info("state is {}", state);

        try {
            callbackDao.insert(callback);
        } catch (SQLException e) {
            callback.setState(CommonUtils.randomString(32));
            try {
                callbackDao.insert(callback);
            } catch (SQLException e1) {
                logger.error(e1.getLocalizedMessage(), e1);
            }
        }

        Map<String, String> param = Maps.newHashMap();
        param.put("appid", ConfigUtils.getRisePcAppid());
        param.put("scope", "snsapi_login");
        param.put("redirect_uri", RISE_PC_OAUTH_URL);
        param.put("state", state);
        param.put("style", "");
        param.put("href", "");
        return param;
    }

    @Override
    public Pair<Integer, Callback> initOpenId(Callback callback) {
        String openid = callback.getPcOpenid();
        String accessToken = callback.getPcAccessToken();
        String url = AccountService.PC_USER_INFO_URL;

        Map<String, String> map = Maps.newHashMap();
        map.put("openid", openid);
        map.put("access_token", accessToken);
        logger.info("请求用户信息,pcOpenid:{}", openid);
        url = CommonUtils.placeholderReplace(url, map);

        String body = restfulHelper.get(url);
        logger.info("请求用户信息结果:{}", body);
        Map<String, Object> result = CommonUtils.jsonToMap(body);

        String errorCode = result.get("errcode").toString();
        if (!StringUtils.isEmpty(errorCode)) {
            logger.info("获取用户信息失败 {}", result.toString());
        }

        String unionId = result.get("unionId").toString();
        // 根据 unionId 查询
        Profile profile = accountService.queryByUnionId(unionId);
        if (profile == null) {
            // 提示关注并选择rise
            logger.info("未关注，请先关注并选择课程,callback:{}", callback);
            return new MutablePair<>(-1, null);
        } else {
            // 是否曾经关注过,现已取关
            String weixinOpenid = profile.getOpenid();
            try {
                accountService.getAccount(weixinOpenid, false);
            } catch (NotFollowingException e) {
                logger.info("未关注，请先关注并选择课程，callback:{}", callback);
                return new MutablePair<>(-1, null);
            }

            // 查到了
            // 更新数据库
            logger.info("更新数据库,account:{}", profile);
            callback.setOpenid(profile.getOpenid());
            callbackDao.updateOpenIdAndUnionId(callback.getState(), profile.getOpenid(), unionId);
            return new MutablePair<>(1, callback);
        }
    }

    private static String getIPFromUrl(String url) {
        Pattern ipPattern = Pattern.compile(IP_REGULAR);
        Matcher matcher = ipPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

}
