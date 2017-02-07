package com.iquanwai.confucius.web.customer.controller;

import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.Region;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.web.customer.dto.AreaDto;
import com.iquanwai.confucius.web.customer.dto.ProfileDto;
import com.iquanwai.confucius.web.customer.dto.RegionDto;
import com.iquanwai.confucius.web.customer.dto.RiseDto;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private PlanService planService;

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadProfile(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户信息不能为空");
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
        // 查询id
        Region city = accountService.loadCityByName(account.getCity());
        Region province = accountService.loadProvinceByName(account.getProvince());
        profileDto.setCityId(city == null ? null : city.getId());
        profileDto.setProvinceId(province == null ? null : province.getId());
        return WebUtils.result(profileDto);
    }

    @RequestMapping(value = "/profile", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitProfile(LoginUser loginUser, @RequestBody ProfileDto profileDto) {
        Assert.notNull(loginUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("个人信息")
                .action("提交个人信息");
        operationLogService.log(operationLog);
        accountService.submitProfile(loginUser.getOpenId(), profileDto.getProvince(), profileDto.getCity(), profileDto.getFunction(), profileDto.getIndustry(), profileDto.getWorkingLife());
        return WebUtils.success();
    }

    @RequestMapping(value = "/profile/region", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Map<String, Object>> submitRegion(LoginUser loginUser, @RequestBody ProfileDto profileDto) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("个人信息")
                .action("提交位置信息");
        operationLogService.log(operationLog);
        accountService.submitRegion(loginUser.getOpenId(), profileDto.getProvince(), profileDto.getCity());
        return WebUtils.success();
    }

    @RequestMapping(value = "/profile/industry", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Map<String, Object>> submitIndustry(LoginUser loginUser, @RequestBody ProfileDto profileDto) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("个人信息")
                .action("提交行业信息");
        operationLogService.log(operationLog);
        accountService.submitIndustry(loginUser.getOpenId(), profileDto.getIndustry());
        return WebUtils.success();
    }

    @RequestMapping(value = "/profile/workinglife", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Map<String, Object>> submitWorkingLife(LoginUser loginUser, @RequestBody ProfileDto profileDto) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("个人信息")
                .action("提交工作年限信息");
        operationLogService.log(operationLog);
        accountService.submitWorkingLife(loginUser.getOpenId(), profileDto.getWorkingLife());
        return WebUtils.success();
    }

    @RequestMapping(value = "/profile/function", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Map<String, Object>> submitFunction(LoginUser loginUser, @RequestBody ProfileDto profileDto) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("个人信息")
                .action("提交职位信息");
        operationLogService.log(operationLog);
        accountService.submitFunction(loginUser.getOpenId(), profileDto.getFunction());
        return WebUtils.success();
    }


    @RequestMapping("/region")
    public ResponseEntity<Map<String, Object>> loadRegion() {
        List<Region> provinces = accountService.loadAllProvinces();
        List<Region> cities = accountService.loadCities();
        RegionDto regionDto = new RegionDto();
        regionDto.setProvinceList(provinces.stream().map(item -> new AreaDto(item.getId() + "", item.getName(), item.getParentId() + "")).collect(Collectors.toList()));
        regionDto.setCityList(cities.stream().map(item -> new AreaDto(item.getId() + "", item.getName(), item.getParentId() + "")).collect(Collectors.toList()));
        return WebUtils.result(regionDto);
    }

    @RequestMapping(value = "/rise",method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadRiseInfo(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("RISE")
                .action("查询rise信息");
        operationLogService.log(operationLog);
        List<ImprovementPlan> plans = planService.loadUserPlans(loginUser.getOpenId());
        RiseDto riseDto = new RiseDto();
        riseDto.setPoint(0);
        plans.forEach(item -> {
            if (item.getPoint() != null) {
                riseDto.setPoint(riseDto.getPoint() + item.getPoint());
            }
        });
        return WebUtils.result(riseDto);
    }

}
