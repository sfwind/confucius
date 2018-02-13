package com.iquanwai.confucius.web.account.controller;

import com.iquanwai.confucius.web.account.dto.AccountDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.resolver.UnionUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/check/follow")
    public ResponseEntity<Map<String, Object>> checkIsFollow(HttpServletRequest request, UnionUser unionUser) {
        // 只要用户有账户信息，就能够访问
        if (unionUser != null) {
            return WebUtils.success();
        } else {
            return WebUtils.error("用户不能为空");
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

