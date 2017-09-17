package com.iquanwai.confucius.web.course.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.backend.MonthlyCampService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.web.course.dto.backend.MonthlyCampDto;
import com.iquanwai.confucius.web.course.dto.backend.MonthlyCampDtoGroup;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by 三十文 on 2017/9/15
 */
@RestController
@RequestMapping("/backend/camp")
public class MonthlyCampController {

    @Autowired
    private MonthlyCampService monthlyCampService;
    @Autowired
    private AccountService accountService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/load", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadMonthlyCampByClassName(@PathParam("className") String className) {
        List<RiseClassMember> riseClassMembers = monthlyCampService.loadMonthlyCampByClassName(className);
        List<Integer> profileIds = riseClassMembers.stream().map(RiseClassMember::getProfileId).collect(Collectors.toList());
        List<Profile> profiles = accountService.getProfiles(profileIds);

        List<MonthlyCampDto> monthlyCampDtos = Lists.newArrayList();
        for (int i = 0; i < riseClassMembers.size(); i++) {
            Integer profileId = riseClassMembers.get(i).getProfileId();
            Profile profile = profiles.stream().filter(item -> profileId.equals(item.getId())).findFirst().get();
            MonthlyCampDto campDto = convertRiseClassMemberToMonthlyCampDto(riseClassMembers.get(i), profile);
            monthlyCampDtos.add(campDto);
        }

        Map<String, List<MonthlyCampDto>> listMap = monthlyCampDtos.stream().collect(Collectors.groupingBy(MonthlyCampDto::getGroupId));

        List<MonthlyCampDtoGroup> monthlyCampDtoGroups = Lists.newArrayList();
        for (Map.Entry<String, List<MonthlyCampDto>> entry : listMap.entrySet()) {
            MonthlyCampDtoGroup monthlyCampDtoGroup = new MonthlyCampDtoGroup();
            monthlyCampDtoGroup.setGroupId(entry.getKey());
            monthlyCampDtoGroup.setMonthlyCampDtos(entry.getValue());
            monthlyCampDtoGroups.add(monthlyCampDtoGroup);
        }
        return WebUtils.result(monthlyCampDtoGroups);
    }

    @RequestMapping(value = "/load/ungroup", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadUnGroupMonthlyCamp() {
        List<RiseClassMember> riseClassMembers = monthlyCampService.loadUnGroupRiseClassMember();
        List<Integer> profileIds = riseClassMembers.stream().map(RiseClassMember::getProfileId).collect(Collectors.toList());
        List<Profile> profiles = accountService.getProfiles(profileIds);

        List<MonthlyCampDto> monthlyCampDtos = Lists.newArrayList();
        for (int i = 0; i < riseClassMembers.size(); i++) {
            Integer profileId = riseClassMembers.get(i).getProfileId();
            Profile profile = profiles.stream().filter(item -> profileId.equals(item.getId())).findFirst().get();
            MonthlyCampDto campDto = convertRiseClassMemberToMonthlyCampDto(riseClassMembers.get(i), profile);
            monthlyCampDtos.add(campDto);
        }
        return WebUtils.result(monthlyCampDtos);
    }

    @RequestMapping(value = "/modify", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> modifyMonthlyCamp(@RequestBody MonthlyCampDto monthlyCampDto) {
        RiseClassMember riseClassMember = new RiseClassMember();
        riseClassMember.setId(monthlyCampDto.getRiseClassMemberId());
        riseClassMember.setClassName(monthlyCampDto.getClassName());
        riseClassMember.setActive(monthlyCampDto.getActive());
        riseClassMember.setGroupId(monthlyCampDto.getGroupId());

        RiseClassMember updatedRiseClassMember = monthlyCampService.modifyMonthlyCampByClassName(riseClassMember);
        if (updatedRiseClassMember != null) {
            Profile profile = accountService.getProfile(updatedRiseClassMember.getProfileId());
            MonthlyCampDto campDto = convertRiseClassMemberToMonthlyCampDto(updatedRiseClassMember, profile);
            return WebUtils.result(campDto);
        } else {
            return WebUtils.error("更新失败");
        }
    }

    /**
     * 将 RiseClassMember 转为 MonthlyCampDto
     */
    private MonthlyCampDto convertRiseClassMemberToMonthlyCampDto(RiseClassMember riseClassMember, Profile profile) {
        MonthlyCampDto campDto = new MonthlyCampDto();
        campDto.setRiseClassMemberId(riseClassMember.getId());
        campDto.setHeadImgUrl(profile.getHeadimgurl());
        campDto.setNickName(profile.getNickname());
        campDto.setRiseId(profile.getRiseId());
        campDto.setClassName(riseClassMember.getClassName());
        String className = riseClassMember.getClassName();
        try {
            int month = Integer.parseInt(className.substring(0, 2));
            int classStr = Integer.parseInt(className.substring(2));
            campDto.setClassNameStr(month + "月" + classStr + "班");
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        campDto.setGroupId(riseClassMember.getGroupId());
        campDto.setMemberId(riseClassMember.getMemberId());
        campDto.setActive(riseClassMember.getActive());
        campDto.setActiveStr(riseClassMember.getActive() == 1 ? "学习中" : "已请假");

        return campDto;
    }

}
