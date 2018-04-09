package com.iquanwai.confucius.web.pc.backend.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.TableDto;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.util.DataUtils;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.pc.backend.dto.UserDto;
import com.iquanwai.confucius.web.resolver.UnionUser;
import com.iquanwai.confucius.web.util.RiseMemberUtils;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pc/operation/user")
public class UserController {

    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AccountService accountService;

    private final Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * 查询用户信息接口
     *
     * @param search
     * @return
     */
    @RequestMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUserInfo(UnionUser unionUser, @RequestParam("searchId") String search) {

        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId()).module("内容运营").action("用户信息").action("查询用户信息");
        operationLogService.log(operationLog);

        List<UserDto> userDtos = Lists.newArrayList();

        Profile profile = accountService.loadProfileByMemberId(search);
        if (profile == null) {
            profile = accountService.getProfileByRiseId(search);
        }
        if (profile == null) {
            if (DataUtils.isInteger(search)) {
                profile = accountService.getProfile(Integer.valueOf(search));
            }
        }
        if (profile == null) {
            //根据昵称查找
            List<Profile> profiles = accountService.loadProfilesByNickName(search);
            profiles.stream().forEach(profile1 -> {
                UserDto userDto = generateUserDto(profile1);
                if (userDto != null) {
                    userDtos.add(userDto);
                }
            });
            return WebUtils.result(userDtos);
        }
        UserDto userDto = generateUserDto(profile);
        if (userDto == null) {
            return WebUtils.error("找不到该用户");
        }
        userDtos.add(userDto);
        return WebUtils.result(userDtos);
    }

    @RequestMapping("/class/search")
    public ResponseEntity<Map<String, Object>> searchUserInfoByClass(UnionUser unionUser, @ModelAttribute Page page, @RequestParam("className") String className, @RequestParam("groupId") String groupId) {
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId()).module("内容运营").action("用户信息").action("查询班级用户信息");
        operationLogService.log(operationLog);

        List<UserDto> userDtos = Lists.newArrayList();
        if (page == null) {
            page = new Page();
        }
        page.setPageSize(20);
        List<RiseClassMember> riseClassMembers;
        //只根据班级查询
        if (StringUtils.isEmpty(groupId)) {
            riseClassMembers = accountService.getByClassName(page, className);
        } else {
            riseClassMembers = accountService.getByClassNameGroupId(page, className, groupId);
        }
        riseClassMembers.stream().forEach(riseClassMember -> {
            Integer profileId = riseClassMember.getProfileId();
            Profile profile = accountService.getProfile(profileId);
            UserDto userDto = generateUserDto(profile);
            if (userDto != null) {
                userDtos.add(userDto);
            }
        });
        TableDto<UserDto> result = new TableDto<>();
        result.setPage(page);
        result.setData(userDtos);
        return WebUtils.result(result);
    }

    /**
     * 生成用户信息
     *
     * @return
     */
    private UserDto generateUserDto(Profile profile) {
        UserDto userDto = new UserDto();
        if (profile == null) {
            return null;
        }
        BeanUtils.copyProperties(profile, userDto);
        RiseClassMember riseClassMember = accountService.getLatestMemberId(profile.getId());
        if (riseClassMember != null) {
            userDto.setMemberId(profile.getMemberId());
            userDto.setClassName(riseClassMember.getClassName());
            userDto.setGroupId(riseClassMember.getGroupId());
        }
        // TODO: 杨仁
        RiseMember riseMember = accountService.getCurrentRiseMember(profile.getId());
        if (riseMember != null) {
            userDto.setOpenDate(riseMember.getOpenDate());
            userDto.setMemberType(RiseMemberUtils.convert(riseMember.getMemberTypeId()));
        }
        return userDto;
    }
}
