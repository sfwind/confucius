package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.Region;
import com.iquanwai.confucius.resolver.LoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.course.dto.InfoSubmitDto;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by justin on 16/10/31.
 */
@RestController
@RequestMapping("/personal")
public class PersonalInfoController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private OperationLogService operationLogService;

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
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("个人信息")
                    .function("编辑个人信息")
                    .action("修改个人信息")
                    .memo(loginUser.getRealName());
            operationLogService.log(operationLog);
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
            //找到名字匹配的省份,设置省份id
            List<Region> regions = accountService.loadAllProvinces();
            Optional<Region> find = regions.stream().filter(region -> region.getName().
                            equals(infoSubmitDto.getProvince())
            ).findFirst();
            if(find.isPresent()) {
                infoSubmitDto.setProvinceId(find.get().getId());
            }else{
                //没找到让用户重新填
                infoSubmitDto.setProvince(null);
                infoSubmitDto.setCity(null);
            }

            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("个人信息")
                    .function("编辑个人信息")
                    .action("加载个人信息")
                    .memo(loginUser.getRealName());
            operationLogService.log(operationLog);
        }catch (Exception e){
            LOGGER.error("加载个人信息失败", e);
            return WebUtils.error("加载个人信息失败");
        }
        return WebUtils.result(infoSubmitDto);
    }

    @RequestMapping(value = "/province/load", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadProvinceInfo(){
        List<Region> region;
        try{
            region = accountService.loadAllProvinces();
        }catch (Exception e){
            LOGGER.error("加载个人信息失败", e);
            return WebUtils.error("加载个人信息失败");
        }
        return WebUtils.result(region);
    }

    @RequestMapping(value = "/city/load/{provinceId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadCities(@PathVariable Integer provinceId,
                                                          LoginUser loginUser){
        List<Region> region;
        try{
            region = accountService.loadCities(provinceId);

            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("个人信息")
                    .function("编辑个人信息")
                    .action("获取城市信息")
                    .memo(provinceId+"");
            operationLogService.log(operationLog);
        }catch (Exception e){
            LOGGER.error("加载个人信息失败", e);
            return WebUtils.error("加载个人信息失败");
        }
        return WebUtils.result(region);
    }
}
