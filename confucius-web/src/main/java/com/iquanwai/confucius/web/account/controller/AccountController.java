package com.iquanwai.confucius.web.account.controller;

import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.web.account.dto.AccountDto;
import com.iquanwai.confucius.web.pc.LoginUserService;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.CookieUtils;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private LoginUserService loginUserService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/check/follow")
    public ResponseEntity<Map<String, Object>> checkIsFollow(HttpServletRequest request, PCLoginUser loginUser) {
        String cookie = CookieUtils.getCookie(request, OAuthService.PC_STATE_COOKIE_NAME);
        if (cookie != null) {
            boolean isFollowing = loginUserService.userIsFollowing(loginUser);
            if (isFollowing) {
                return WebUtils.success();
            } else {
                return WebUtils.error(403, "当前用户尚未关注服务号");
            }
        } else {
            return WebUtils.error(401, "当前用户未登录");
        }
    }

    @RequestMapping("/get")
    public ResponseEntity<Map<String, Object>> getAccount(PCLoginUser pcLoginUser) {
        try {
            Assert.notNull(pcLoginUser, "用户不能为空");
            AccountDto accountDto = new AccountDto();
            accountDto.setWeixinName(pcLoginUser.getWeixin().getWeixinName());
            accountDto.setHeadimgUrl(pcLoginUser.getWeixin().getHeadimgUrl());
            accountDto.setRole(pcLoginUser.getRole());
            return WebUtils.result(accountDto);
        } catch (Exception err) {
            logger.error("获取用户信息失败", err.getLocalizedMessage());
            return WebUtils.error("获取用户信息失败");
        }
    }
}

