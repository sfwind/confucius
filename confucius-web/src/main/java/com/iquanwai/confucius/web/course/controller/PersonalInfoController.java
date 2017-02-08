package com.iquanwai.confucius.web.course.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.customer.ProfileService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.Region;
import com.iquanwai.confucius.biz.po.customer.Profile;
import com.iquanwai.confucius.web.course.dto.InfoSubmitDto;
import com.iquanwai.confucius.web.course.dto.ProvinceDto;
import com.iquanwai.confucius.web.course.dto.RegionDto;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.collections.CollectionUtils;
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
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Autowired
    private ProfileService profileService;

    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/info/submit", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateInfo(@RequestBody InfoSubmitDto infoSubmitDto,
                         LoginUser loginUser){
        try{
            Assert.notNull(loginUser, "用户不能为空");
            if(infoSubmitDto.getRealName()==null){
                return WebUtils.error("请填写姓名");
            }
            Profile account = new Profile();
            ModelMapper mapper = new ModelMapper();
            mapper.map(infoSubmitDto, account);
            account.setOpenid(loginUser.getOpenId());
            profileService.submitPersonalInfo(account);
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("个人信息")
                    .function("编辑个人信息")
                    .action("修改个人信息");
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
            Profile account = profileService.getProfile(loginUser.getOpenId());
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
                infoSubmitDto.setProvince("请选择");
                infoSubmitDto.setCity(null);
            }

            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("个人信息")
                    .function("编辑个人信息")
                    .action("加载个人信息");
            operationLogService.log(operationLog);
        }catch (Exception e){
            LOGGER.error("加载个人信息失败", e);
            return WebUtils.error("加载个人信息失败");
        }
        return WebUtils.result(infoSubmitDto);
    }

    @RequestMapping(value = "/province/load", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadProvinceInfo(){
        ProvinceDto provinceDto = new ProvinceDto();
        try{
            List<Region> province = accountService.loadAllProvinces();
            province.add(Region.defaultRegion());
            Map<Integer, Region> provinceMap = Maps.newHashMap();
            List<RegionDto> regionDtos = province.stream().map(region -> {
                provinceMap.put(region.getId(), region);
                return new RegionDto().id(region.getId()).name(region.getName());
            }).collect(Collectors.toList());

            provinceDto.setProvince(regionDtos);
            List<Region> city = accountService.loadCities();
            city.stream().forEach(region -> {
                Region p = provinceMap.get(region.getParentId());
                List<RegionDto> cityList = provinceDto.getCity().get(p.getName());
                if (CollectionUtils.isEmpty(cityList)) {
                    cityList = Lists.newArrayList();
                    provinceDto.getCity().put(p.getName(), cityList);
                }
                cityList.add(new RegionDto().id(region.getId()).name(region.getName()));
            });
        }catch (Exception e){
            LOGGER.error("加载个人信息失败", e);
            return WebUtils.error("加载个人信息失败");
        }
        return WebUtils.result(provinceDto);
    }

}
