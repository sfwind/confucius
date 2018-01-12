package com.iquanwai.confucius.web.account.controller;

import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.web.account.dto.AccountDto;
import com.iquanwai.confucius.web.account.dto.UserInfo;
import com.iquanwai.confucius.web.pc.LoginUserService;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.resolver.WeMiniLoginUser;
import com.iquanwai.confucius.web.util.CookieUtils;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by nethunder on 2016/12/20.
 */
@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private LoginUserService loginUserService;
    @Autowired
    private AccountService accountService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/check/follow")
    public ResponseEntity<Map<String, Object>> checkIsFollow(HttpServletRequest request, PCLoginUser loginUser) {
        String cookie = CookieUtils.getCookie(request, OAuthService.QUANWAI_TOKEN_COOKIE_NAME);
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

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> initWeMiniUserInfo(WeMiniLoginUser weMiniLoginUser, UserInfo userInfo) {
        Assert.notNull(weMiniLoginUser);
        String unionId = weMiniLoginUser.getUnionId();
        String nickName = userInfo.getNickName();
        String avatarUrl = userInfo.getAvatarUrl();
        Integer gender = userInfo.getGender();
        int result = accountService.initProfileAndFollowUser(unionId, nickName, avatarUrl, gender);
        if (result > 0) {
            return WebUtils.success();
        } else {
            return WebUtils.error("用户信息更新失败");
        }
    }
}

