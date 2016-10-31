package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.resolver.LoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.course.dto.InfoSubmitDto;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by justin on 16/10/31.
 */
@RestController
@RequestMapping("/personal")
public class PersonalInfoController {
    @Autowired
    private AccountService accountService;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/info/submit", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateInfo(@RequestBody InfoSubmitDto infoSubmitDto,
                         LoginUser loginUser){
        try{
            Assert.notNull(loginUser, "用户不能为空");
            Account account = new Account();
            ModelMapper mapper = new ModelMapper();
            mapper.map(infoSubmitDto, account);
            account.setOpenid(loginUser.getOpenId());
            accountService.submitPersonalInfo(account);
        }catch (Exception e){
            LOGGER.error("提交个人信息失败", e);
            return WebUtils.error("提交个人信息失败");
        }
        return WebUtils.success();
    }

    @RequestMapping(value = "/info/load", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadInfo(LoginUser loginUser){
        InfoSubmitDto infoSubmitDto = new InfoSubmitDto();
        try{
            Assert.notNull(loginUser, "用户不能为空");
            Account account = accountService.getAccount(loginUser.getOpenId(), false);
            ModelMapper mapper = new ModelMapper();
            mapper.map(account, infoSubmitDto);
        }catch (Exception e){
            LOGGER.error("加载个人信息失败", e);
            return WebUtils.error("加载个人信息失败");
        }
        return WebUtils.result(infoSubmitDto);
    }
}
