package com.iquanwai.confucius.web.weixin;

import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.api.WeiXinApiService;
import com.iquanwai.confucius.biz.domain.weixin.api.WeiXinResult;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.web.resolver.UnionUser;
import com.iquanwai.confucius.web.resolver.UnionUserService;
import com.iquanwai.confucius.web.util.WebUtils;
import com.iquanwai.confucius.web.weixin.dto.WeMiniCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

@RequestMapping("/wx/oauth")
@Controller
public class OAuthController {

    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private WeiXinApiService weiXinApiService;
    @Autowired
    private UnionUserService unionUserService;
    @Autowired
    private AccountService accountService;

    public static final String ERROR_STATE_SUFFIX = "#wechat_redirect";
    public static final String PAGE_NOT_FOUND = "/403.jsp";

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 拼装用户授权页面
     */
    @RequestMapping("/auth")
    public void oauthCode(@RequestParam("callbackUrl") String callbackUrl, HttpServletRequest request, HttpServletResponse response) {
        try {
            String remoteIp = request.getHeader("X-Forwarded-For");
            String state = CommonUtils.randomString(32);
            // 数据库预存储 callback 对象
            oAuthService.initCallback(callbackUrl, state);
            String redirectOAuthUrl = weiXinApiService.generateRedirectOAuthUrl(state, "/wx/oauth/code");
            if (ConfigUtils.logDetail()) {
                logger.info("ip is {}, callbackUrl is {}, requestUrl is {}", remoteIp, callbackUrl, redirectOAuthUrl);
            }
            response.sendRedirect(redirectOAuthUrl);
        } catch (Exception e) {
            logger.error("auth failed", e);
            try {
                response.sendRedirect(PAGE_NOT_FOUND);
            } catch (IOException e1) {
                // ignore
            }
        }
    }

    /**
     * 用户成功授权，微信回调接口
     * @param code 微信返回 code
     * @param state 在构建授权页面时，携带给微信的 state，微信会一样返回，为 callback 中 state 的值
     */
    @RequestMapping("/code")
    public void oauthCode(@RequestParam(required = false) String code, @RequestParam String state, HttpServletResponse response) {
        try {
            if (code == null) {
                // 用户不同意授权,跳转报错页面
                logger.error("code interface error , code is null,state is {}", state);
                response.sendRedirect(PAGE_NOT_FOUND);
                return;
            }
            // 根据 code 去换取微信 accessToken
            WeiXinResult.UserAccessTokenObject userAccessTokenObject = weiXinApiService.exchangeUserAccessTokenByCode(code);
            if (state != null && state.endsWith(ERROR_STATE_SUFFIX)) {
                state = state.replace(ERROR_STATE_SUFFIX, "");
            }
            Callback callback = oAuthService.supplementMobileCallback(state, userAccessTokenObject);
            if (callback == null) {
                response.sendRedirect(PAGE_NOT_FOUND);
            } else {
                // callback 存在，根据 accessToken 获取用户数据，并填充到 Profile 和 FollowUser
                WeiXinResult.UserInfoObject userInfoObject = accountService.storeWeiXinUserInfo(callback.getOpenid(), callback.getAccessToken(), Profile.ProfileType.MOBILE);
                oAuthService.supplementCallbackUnionId(state, userInfoObject.getUnionId());
                // 在用户 cookie 中写入 state
                logger.info("set _act {} for {} ", callback.getAccessToken(), callback.getOpenid());
                unionUserService.addCookie(UnionUser.Platform.MOBILE, state, response);
                response.sendRedirect(callback.getCallbackUrl());
            }
        } catch (Exception e) {
            logger.error("code failed", e);
            try {
                response.sendRedirect(PAGE_NOT_FOUND);
            } catch (IOException e1) {
                // ignore
            }
        }
    }

    /**
     * pc 端如果未登录，跳转登录页面，会访问这个链接
     * @param callbackUrl 成功之后，最后跳转 url
     */
    @ResponseBody
    @RequestMapping("/pc/auth")
    public ResponseEntity<Map<String, Object>> pcOAuthCode(@RequestParam("callbackUrl") String callbackUrl, HttpServletRequest request, HttpServletResponse response) {
        try {
            String remoteIp = request.getHeader("X-Forwarded-For");
            String state = CommonUtils.randomString(32);
            // 数据库预存储 callback 对象
            oAuthService.initCallback(callbackUrl, state);
            Map<String, String> authParam = weiXinApiService.generateJsOAuthParam(state, "/wx/oauth/pc/code");
            if (ConfigUtils.logDetail()) {
                logger.info("ip is {}, callbackUrl is {}, param is {}", remoteIp, callbackUrl, authParam);
            }
            return WebUtils.result(authParam);
        } catch (Exception e) {
            logger.error("auth failed", e);
            try {
                response.sendRedirect(PAGE_NOT_FOUND);
            } catch (IOException e1) {
                // ignore
            }
        }
        return null;
    }

    @RequestMapping("/pc/code")
    public void pcOAuthCode(@RequestParam(required = false) String code, @RequestParam String state, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (code == null) {
                // 用户不同意授权,跳转报错页面
                logger.error("code interface error , code is null,state is {}", state);
                response.sendRedirect(PAGE_NOT_FOUND);
                return;
            }
            // 根据 code 去换取微信 accessToken
            WeiXinResult.UserAccessTokenObject userAccessTokenObject = weiXinApiService.exchangeUserAccessTokenByCode(code);
            if (state != null && state.endsWith(ERROR_STATE_SUFFIX)) {
                state = state.replace(ERROR_STATE_SUFFIX, "");
            }
            Callback callback = oAuthService.supplementPcCallback(state, userAccessTokenObject);
            if (callback == null) {
                response.sendRedirect(PAGE_NOT_FOUND);
            } else {
                // 存储 Profile、FollowUser
                WeiXinResult.UserInfoObject userInfoObject = accountService.storeWeiXinUserInfo(callback.getPcOpenid(), callback.getPcAccessToken(), Profile.ProfileType.PC);
                // 完善 Callback 表中的 UnionId
                oAuthService.supplementCallbackUnionId(state, userInfoObject.getUnionId());
                // 在用户 cookie 中写入 state
                logger.info("set _act {} for {} ", callback.getAccessToken(), callback.getOpenid());
                unionUserService.addCookie(UnionUser.Platform.PC, state, response);
                response.sendRedirect(callback.getCallbackUrl());
            }
        } catch (Exception e) {
            logger.error("code failed", e);
            try {
                response.sendRedirect(PAGE_NOT_FOUND);
            } catch (IOException e1) {
                // ignore
            }
        }
    }

    @RequestMapping("/mini/code")
    public ResponseEntity<Map<String, Object>> oauthWeMiniCode(@RequestParam(value = "code") String code) {
        try {
            WeiXinResult.UserAccessTokenObject userAccessTokenObject = weiXinApiService.exchangeUserAccessTokenByCode(code);

            String state = CommonUtils.randomString(32);
            Callback callback = oAuthService.supplementPcCallback()

            Callback callback = oAuthService.weMiniAccessToken(code);
            Profile profile = accountService.getProfileByUnionId(callback.getUnionId());
            Assert.notNull(callback, "callback 数据不能为空");

            WeMiniCallback weMiniCallback = new WeMiniCallback();
            weMiniCallback.setState(callback.getState());
            weMiniCallback.setExpireDate(DateUtils.afterDays(new Date(), 7).getTime());
            weMiniCallback.setFirstLogin(profile == null);
            return WebUtils.result(weMiniCallback);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
            return WebUtils.error("服务器伐开心,我们正在想办法");
        }
    }

    /**
     * 对于其他平台，如 platon，resolver 中如果没有获取到 UnionUser，调用此接口，成功返回之后，再次获取用户对象
     */
    @RequestMapping("/init/user")
    public ResponseEntity<Map<String, Object>> generateUnionUser(@RequestParam("state") String state) {
        Callback callback = oAuthService.getCallbackByState(state);
        Profile profile = accountService.getProfileByUnionId(callback.getUnionId());
        if (profile != null) {
            return WebUtils.success();
        } else {
            return WebUtils.error("刷新用户 Profile 对象失败，state：" + state);
        }
    }

    @RequestMapping("/openid/{access_token}")
    public ResponseEntity<Map<String, Object>> openid(@PathVariable("access_token") String accessToken) {
        try {
            String openid = oAuthService.openId(accessToken);
            logger.info("openId {}, accesstoken {}", openid, accessToken);
            return WebUtils.result(openid);
        } catch (Exception e) {
            logger.error("openid failed", e);
        }
        return WebUtils.error("accesstoken is expired");
    }

    @RequestMapping("/refresh/{access_token}")
    public ResponseEntity<Map<String, Object>> refresh(@PathVariable("access_token") String accessToken) {
        try {
            String newAccessToken = oAuthService.refresh(accessToken);
            return WebUtils.result(newAccessToken);
        } catch (Exception e) {
            logger.error("refresh failed", e);
        }
        return WebUtils.error("refresh failed");
    }

}
