package com.iquanwai.confucius.web.personal.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.Region;
import com.iquanwai.confucius.web.personal.dto.AreaDto;
import com.iquanwai.confucius.web.personal.dto.ProfileDto;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2017/2/4.
 */
@RestController
@RequestMapping("/customer")
public class CustomerController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountService accountService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping("/profile")
    public ResponseEntity<Map<String, Object>> loadProfile(LoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("个人信息")
                .action("加载个人信息");
        operationLogService.log(operationLog);
        ProfileDto profileDto = new ProfileDto();
        Account account = accountService.getAccount(loginUser.getOpenId(), false);
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.map(account, profileDto);
        profileDto.isFullCheck();
        return WebUtils.result(profileDto);
    }


    @RequestMapping("/region")
    public ResponseEntity<Map<String, Object>> loadRegion() {
        List<Region> provinces = accountService.loadAllProvinces();
        provinces.add(Region.defaultRegion());
        Map<Integer, AreaDto> provinceMap = Maps.newHashMap();
        provinces.forEach(region -> provinceMap.put(region.getId(), new AreaDto(region.getName(), Lists.newArrayList())));
        List<Region> cities = accountService.loadCities();
        cities.forEach(item -> {
            try {
                provinceMap.get(item.getParentId()).getSub().add(new AreaDto(item.getName()));
            } catch (NullPointerException e) {
                logger.error("设置城市失败", e);
            }
        });
        return WebUtils.result(provinceMap.values());
    }

}
