package com.iquanwai.confucius.web.weixin;

import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.po.Callback;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.Role;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.web.pc.LoginUserService;
import com.iquanwai.confucius.web.util.CookieUtils;
import com.iquanwai.confucius.web.util.WebUtils;
import com.iquanwai.confucius.web.weixin.dto.WeMiniCallback;
import org.apache.commons.lang3.tuple.Pair;
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

/**
 * Created by justin on 8/14/14.
 */
@RequestMapping("/wx/oauth")
@Controller
public class OAuthController {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final String ERROR_STATE_SUFFIX = "#wechat_redirect";

    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private LoginUserService loginUserService;
    @Autowired
    private AccountService accountService;

    @RequestMapping("/auth")
    public void oauthCode(@RequestParam("callbackUrl") String callbackUrl, HttpServletRequest request, HttpServletResponse response) {
        try {
            String remoteIp = request.getHeader("X-Forwarded-For");

            String requestUrl = oAuthService.redirectUrl(callbackUrl, OAuthService.OAUTH_URL);
            if (ConfigUtils.logDetail()) {
                logger.info("ip is {},callbackUrl is {},requestUrl is {}", remoteIp, callbackUrl, requestUrl);
            }
            response.sendRedirect(requestUrl);

        } catch (Exception e) {
            logger.error("auth failed", e);
            try {
                response.sendRedirect("/403.jsp");
            } catch (IOException e1) {
                // ignore
            }
        }
    }

    @RequestMapping("/auth/ask")
    public void oauthAskCode(@RequestParam("callbackUrl") String callbackUrl, HttpServletRequest request, HttpServletResponse response) {
        try {
            String remoteIp = request.getHeader("X-Forwarded-For");

            String requestUrl = oAuthService.redirectUrl(callbackUrl, OAuthService.OAUTH_ASK_URL);
            if (ConfigUtils.logDetail()) {
                logger.info("ip is {},callbackUrl is {},requestUrl is {}", remoteIp, callbackUrl, requestUrl);
            }
            response.sendRedirect(requestUrl);

        } catch (Exception e) {
            logger.error("auth failed", e);
            try {
                response.sendRedirect("/403.jsp");
            } catch (IOException e1) {
                // ignore
            }
        }
    }

    @RequestMapping("/code")
    public void oauthCode(@RequestParam(required = false) String code, @RequestParam String state, HttpServletResponse response) {
        try {
            if (code == null) {
                //用户不同意授权,跳转报错页面
                logger.error("code interface error , code  is null,state is {}", state);
                return;
            }

            if (state != null && state.endsWith(ERROR_STATE_SUFFIX)) {
                state = state.replace(ERROR_STATE_SUFFIX, "");
            }

            // 返回带accessToken的url
            Callback callback = oAuthService.accessToken(code, state);
            if (callback == null) {
                response.sendRedirect("/403.jsp");
            } else {
                logger.info("set _act {} for {} ", callback.getAccessToken(), callback.getOpenid());
                //在cookie中写入access_token
                CookieUtils.addCookie(OAuthService.ACCESS_TOKEN_COOKIE_NAME,
                        callback.getAccessToken(), OAuthService.SEVEN_DAYS, response);
                response.sendRedirect(callback.getCallbackUrl());
            }
        } catch (Exception e) {
            logger.error("code failed", e);
            try {
                response.sendRedirect("/403.jsp");
            } catch (IOException e1) {
                // ignore
            }
        }
    }

    @RequestMapping("/code/ask")
    public void oauthAskCode(@RequestParam(required = false) String code, @RequestParam String state, HttpServletResponse response) {
        try {
            if (code == null) {
                //用户不同意授权,跳转报错页面
                logger.error("code interface error , code  is null,state is {}", state);
                return;
            }

            if (state != null && state.endsWith(ERROR_STATE_SUFFIX)) {
                state = state.replace(ERROR_STATE_SUFFIX, "");
            }

            // 返回带accessToken的url
            Callback callback = oAuthService.accessToken(code, state);
            if (callback == null) {
                response.sendRedirect("/403.jsp");
            } else {
                logger.info("set _ask {} for {} ", callback.getAccessToken(), callback.getOpenid());
                //在cookie中写入access_token
                CookieUtils.addCookie(OAuthService.ACCESS_ASK_TOKEN_COOKIE_NAME,
                        callback.getAccessToken(), OAuthService.SEVEN_DAYS, response);
                response.sendRedirect(callback.getCallbackUrl());
            }
        } catch (Exception e) {
            logger.error("code failed", e);
            try {
                response.sendRedirect("/403.jsp");
            } catch (IOException e1) {
                // ignore
            }
        }
    }

    @RequestMapping("/mini/code")
    public ResponseEntity<Map<String, Object>> oauthWeMiniCode(@RequestParam(value = "code") String code) {
        try {
            Callback callback = oAuthService.weMiniAccessToken(code);
            Profile profile = accountService.queryByUnionId(callback.getUnionId());
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

    @RequestMapping("/pc/auth")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> pcOAuthCode(@RequestParam("callbackUrl") String callbackUrl, HttpServletRequest request, HttpServletResponse response) {
        try {
            String remoteIp = request.getHeader("X-Forwarded-For");

            Map<String, String> authParam = oAuthService.pcRedirectUrl(callbackUrl);
            if (ConfigUtils.logDetail()) {
                logger.info("ip is {},callbackUrl is {},param is {}", remoteIp, callbackUrl, authParam);
            }
            return WebUtils.result(authParam);
        } catch (Exception e) {
            logger.error("auth failed", e);
            try {
                response.sendRedirect("/403.jsp");
            } catch (IOException e1) {
                // ignore
            }
        }
        return null;
    }

    @RequestMapping("/pc/code")
    public void pcOAuthCode(@RequestParam(required = false) String code, @RequestParam String state, HttpServletRequest request, HttpServletResponse response) {

        try {
            String remoteIp = request.getHeader("X-Forwarded-For");
            logger.info("remoteIp:{} ask code", remoteIp);
            Callback callback = oAuthService.pcAccessToken(code, state);
            if (callback == null) {
                response.sendRedirect("/403.jsp");
                return;
            }
            // 根据openid，accessToken换取unionid，根据unionid来获取
            Pair<Integer, Callback> pair = oAuthService.initOpenId(callback);
            if (pair.getLeft() == -1) {
                // 提示关注并选择课程
                CookieUtils.removeCookie(OAuthService.QUANWAI_TOKEN_COOKIE_NAME,
                        response);
                response.sendRedirect("/servercode");
            } else {
                Role userRole = loginUserService.getUserRole(callback.getOpenid());
                if (userRole.getLevel().equals(0)) {
                    // 选择课程
                    logger.info("state:{},openid:{},提示开始训练", state, callback.getOpenid());
                    CookieUtils.removeCookie(OAuthService.QUANWAI_TOKEN_COOKIE_NAME,
                            response);
                    response.sendRedirect("/servercode");
                    return;
                }
                // 进行跳转
                // 返回带accessToken的url
                logger.info("set _act {} for {} ", callback.getPcAccessToken(), callback.getOpenid());
                //在cookie中写入access_token
                CookieUtils.addCookie(OAuthService.QUANWAI_TOKEN_COOKIE_NAME,
                        callback.getPcAccessToken(), OAuthService.SEVEN_DAYS, response);
                response.sendRedirect(callback.getCallbackUrl());
            }
        } catch (Exception e) {
            logger.error("auth failed", e);
            try {
                response.sendRedirect("/403.jsp");
            } catch (IOException e1) {
                // ignore
            }
        }
    }

}
