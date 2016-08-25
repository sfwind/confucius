package com.iquanwai.confucius.web.weixin;

import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by justin on 8/14/14.
 */
@RequestMapping("/oauth")
@Controller
public class OAuthController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private OAuthService oAuthService;

    @RequestMapping("/auth")
    public void oauthCode(@RequestParam String callbackUrl,
                          HttpServletResponse response) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("callbackUrl is " + callbackUrl);
            }
            String requestUrl = oAuthService.redirectUrl(callbackUrl);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("requestUrl is " + requestUrl);
            }
            response.sendRedirect(requestUrl);

        }catch (Exception e){
            LOGGER.error("auth failed", e);
            try {
                response.sendRedirect("/403.jsp");
            } catch (IOException e1) {
                // ignore
            }
        }
    }

    @RequestMapping("/code")
    public void oauthCode(@RequestParam(required=false) String code,
                            @RequestParam String state,
                            HttpServletResponse response) {
        try {
            if (code == null) {
                //用户不同意授权,跳转报错页面
                return;
            }

            // 返回带accessToken的url
            String callbackUrl = oAuthService.accessToken(code, state);
            if(callbackUrl==null){
                response.sendRedirect("/403.jsp");
            }else {
                response.sendRedirect(callbackUrl);
            }
        }catch (Exception e){
            LOGGER.error("code failed", e);
            try {
                response.sendRedirect("/403.jsp");
            } catch (IOException e1) {
                // ignore
            }
        }
    }

    @RequestMapping("/openid/{access_token}")
    public ResponseEntity<Map<String, Object>> openid(@PathVariable("access_token") String accessToken) {
        try {
            String openid = oAuthService.openId(accessToken);
            return WebUtils.result(openid);
        }catch (Exception e){
            LOGGER.error("openid failed", e);
        }
        return WebUtils.error("accessToken is expired");
    }

    @RequestMapping("/refresh/{access_token}")
    public ResponseEntity<Map<String, Object>> refresh(@PathVariable("access_token") String accessToken) {
        try {
            String newAccessToken = oAuthService.refresh(accessToken);
            return WebUtils.result(newAccessToken);
        }catch (Exception e){
            LOGGER.error("refresh failed", e);
        }
        return WebUtils.error("refresh failed");
    }


}
