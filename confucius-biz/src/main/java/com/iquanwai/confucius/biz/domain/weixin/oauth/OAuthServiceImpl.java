package com.iquanwai.confucius.biz.domain.weixin.oauth;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.wx.CallbackDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.api.WeiXinResult;
import com.iquanwai.confucius.biz.exception.NotFollowingException;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import okhttp3.ResponseBody;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OAuthServiceImpl implements OAuthService {

    // String OAUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid={appid}&redirect_uri={redirect_url}&response_type=code&scope=snsapi_userinfo&state={state}#wechat_redirect";
    // String OAUTH_ASK_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid={appid}&redirect_uri={redirect_url}&response_type=code&scope=snsapi_userinfo&state={state}#wechat_redirect";
    // String REFRESH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid={appid}&grant_type=refresh_token&refresh_token={refresh_token}";
    // String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid={appid}&secret={secret}&code={code}&grant_type=authorization_code";
    // String WE_MINI_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/jscode2session?appid={APPID}&secret={SECRET}&js_code={JSCODE}&grant_type=authorization_code";
    //
    // String RISE_PC_OAUTH_URL = ConfigUtils.domainName() + "/wx/oauth/pc/code";
    //
    // String WE_CHAT_STATE_COOKIE_NAME = "_act";
    // String PC_STATE_COOKIE_NAME = "_qt";
    // String ACCESS_ASK_TOKEN_COOKIE_NAME = "_ask";
    //
    // int SEVEN_DAYS = 60 * 60 * 24 * 7;

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

    /** 引导用户授权，在回调接口中返回 code 值 */
    String OAUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid={appid}&redirect_uri={redirect_url}&response_type=code&scope=snsapi_userinfo&state={state}#wechat_redirect";

    /** 获取当前用户的 accessToken */
    String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid={appid}&secret={secret}&code={code}&grant_type=authorization_code";

    /** 当前用户 accessToken 过期时，通过 refreshToken 获取新 accessToken 和 refreshToken */
    String REFRESH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid={appid}&grant_type=refresh_token&refresh_token={refresh_token}";

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

    String RISE_PC_OAUTH_URL = ConfigUtils.domainName() + "/wx/oauth/pc/code";

    int SEVEN_DAYS = 60 * 60 * 24 * 7;

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
    public String redirectUrl(String callbackUrl, String authUrl) {
        String requestUrl = authUrl;
        Callback callback = new Callback();
        String ip = getIpFromUrl(callbackUrl);
        if (ip != null) {
            callbackUrl = callbackUrl.replace("http://" + ip, ConfigUtils.domainName());
        }
        callback.setCallbackUrl(callbackUrl);
        String state = CommonUtils.randomString(32);
        callback.setState(state);
        callbackDao.insert(callback);
        logger.info("state is {}", callback.getState());

        Map<String, String> params = Maps.newHashMap();
        params.put("appid", ConfigUtils.getAppid());
        params.put("scope", "snsapi_login,snsapi_userinfo");

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
        Map<String, Object> userInfoResult = getUserInfoFromWeiXin(openid, accessToken);
        String unionId = userInfoResult.get("unionid").toString();
        callbackDao.updateUserInfo(state, accessToken, refreshToken, openid, unionId);
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
        Map<String, Object> userInfoResult = getUserInfoFromWeiXin(openid, accessToken);
        String unionId = userInfoResult.get("unionid").toString();
        callbackDao.updatePcUserInfo(state, accessToken, refreshToken, openid, unionId);
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
        insertResult = callbackDao.insert(callback);
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
        String ip = getIpFromUrl(callbackUrl);
        if (ip != null) {
            callbackUrl = callbackUrl.replace("http://" + ip, ConfigUtils.domainName());
        }
        callback.setCallbackUrl(callbackUrl);
        String state = CommonUtils.randomString(32);
        callback.setState(state);
        logger.info("state is {}", state);
        callback.setState(CommonUtils.randomString(32));
        Map<String, String> param = Maps.newHashMap();
        param.put("appid", ConfigUtils.getRisePcAppid());
        param.put("scope", "snsapi_login,snsapi_userinfo");
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

        Map<String, Object> result = getUserInfoFromWeiXin(openid, accessToken);
        String unionId = result.get("unionid").toString();
        // 根据 unionId 查询
        Profile profile = accountService.getProfileByUnionId(unionId);
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
            callbackDao.updateOpenId(callback.getState(), profile.getOpenid());
            return new MutablePair<>(1, callback);
        }
    }

    /**
     * 从微信获取用户基本信息
     * @param openId 各个平台对应 openid
     */
    private Map<String, Object> getUserInfoFromWeiXin(String openId, String accessToken) {
        String url = AccountService.SNS_API_USER_INFO;
        Map<String, String> map = Maps.newHashMap();
        map.put("openid", openId);
        map.put("access_token", accessToken);
        logger.info("请求用户信息, openid:{}", openId);
        url = CommonUtils.placeholderReplace(url, map);
        String body = restfulHelper.get(url);
        logger.info("请求用户信息结果: {}", body);
        Map<String, Object> result = CommonUtils.jsonToMap(body);
        Object errorCode = result.get("errcode");
        if (errorCode != null) {
            logger.info("获取用户信息失败 {}", result.toString());
            return Maps.newHashMap();
        }
        return result;
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
