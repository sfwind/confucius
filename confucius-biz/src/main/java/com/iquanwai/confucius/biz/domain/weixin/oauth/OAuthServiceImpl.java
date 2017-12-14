package com.iquanwai.confucius.biz.domain.weixin.oauth;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.wx.CallbackDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by justin on 16/8/13.
 */
@Service
public class OAuthServiceImpl implements OAuthService {
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private CallbackDao callbackDao;
    @Autowired
    private ProfileDao profileDao;

    private static final String REDIRECT_PATH = "/wx/oauth/code";

    private static final String REDIRECT_ASK_PATH = "/wx/oauth/code";

    private static final String IP_REGULAR = "(\\d*\\.){3}\\d*";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String redirectUrl(String callbackUrl, String authUrl, String domainName) {
        String requestUrl = authUrl;
        Callback callback = new Callback();
        String ip = getIPFromUrl(callbackUrl);
        if (ip != null) {
            if (domainName == null) {
                callbackUrl = callbackUrl.replace("http://" + ip, ConfigUtils.adapterDomainName());
            } else {
                logger.info("domain name is {}", domainName);
                callbackUrl = callbackUrl.replace("http://" + ip, "http://" + domainName);
            }
        }
        callback.setCallbackUrl(callbackUrl);
        String state = CommonUtils.randomString(32);
        callback.setState(state);
        logger.info("state is {}", state);
        callbackDao.insert(callback);

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
            logger.error("accesstoken {} is invalid", accessToken);
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
            logger.error("accesstoken {} is invalid", accessToken);
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

        //刷新accessToken
        callbackDao.refreshToken(callback.getState(), newAccessToken);
        return newAccessToken;
    }

    /**
     * 新增pc获取accessToken
     */
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
        //更新accessToken，refreshToken，openid
        logger.info("update callback, state:{},pcAccessToken:{},refreshToken:{},pcOpenId:{},code:{}", state, accessToken, refreshToken, openid, code);
        // pc登录，先将用户的openid存下来
        callback.setPcOpenid(openid);
        callback.setRefreshToken(refreshToken);
        callback.setPcAccessToken(accessToken);
        callbackDao.updatePcUserInfo(state, accessToken, refreshToken, openid);

        // callbackUrl增加参数access_token
//        String callbackUrl = callback.getCallbackUrl();
//        callbackUrl = CommonUtils.appendAccessToken(callbackUrl, accesstoken);
        return callback;
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
        //更新accessToken，refreshToken，openid
        callback.setOpenid(openid);
        callback.setRefreshToken(refreshToken);
        callback.setAccessToken(accessToken);
        logger.info("update callback, state:{},accesstoken:{},refreshToken:{},openId:{},code:{}", state, accessToken, refreshToken, openid, code);
        callbackDao.updateUserInfo(state, accessToken, refreshToken, openid);

        return callback;
    }

    public static String getIPFromUrl(String url) {
        Pattern ipPattern = Pattern.compile(IP_REGULAR);
        Matcher matcher = ipPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    /**
     * 根据请求中的回调 URI，拼凑出用于生成微信二维码的参数
     */
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
        callbackDao.insert(callback);
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
        Account account = new Account();
        try {
            BeanUtils.populate(account, result);
        } catch (Exception e) {
            logger.info("获取用户信息失败 {}", e);
            return null;
        }
        //根据unionId查询
        Profile profile = profileDao.queryByUnionId(account.getUnionid());
        if (profile == null) {
            // 提示关注并选择rise
            logger.info("未关注，请先关注并选择课程,callback:{}", callback);
            return new MutablePair<>(-1, null);
        } else {
            // 查到了
            // 更新数据库
            logger.info("更新数据库,account:{}", profile);
            callback.setOpenid(profile.getOpenid());
            callbackDao.updateOpenId(callback.getState(), profile.getOpenid());
            return new MutablePair<>(1, callback);
        }
    }

}
