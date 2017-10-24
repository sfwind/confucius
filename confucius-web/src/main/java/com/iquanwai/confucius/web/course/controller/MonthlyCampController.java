package com.iquanwai.confucius.web.course.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.backend.MonthlyCampService;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.fragmentation.CacheService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.MonthlyCampConfig;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.course.dto.backend.MonthlyCampDto;
import com.iquanwai.confucius.web.course.dto.backend.MonthlyCampDtoGroup;
import com.iquanwai.confucius.web.course.dto.backend.MonthlyCampPageDto;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private SignupService signupService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private OperationLogService operationLogService;

    private static final int PAGE_SIZE = 20;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/load", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadMonthlyCampByClassName(@PathParam("className") String className) {
        List<RiseClassMember> riseClassMembers = monthlyCampService.loadRiseClassMemberByClassName(className);
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

    @RequestMapping(value = "/load/profile/{type}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadProfile(@PathVariable("type") String type,
                                                           @PathParam("nickName") String nickName,
                                                           @PathParam("riseId") String riseId,
                                                           @PathParam("memberId") String memberId) {
        List<Profile> profiles = Lists.newArrayList();
        switch (type) {
            case "nickName":
                if (!StringUtils.isEmpty(nickName)) {
                    List<Profile> profileList = accountService.loadProfilesByNickName(nickName);
                    if (profileList.size() > 0) {
                        profiles.addAll(accountService.loadProfilesByNickName(nickName));
                    }
                }
                break;
            case "riseId":
                if (!StringUtils.isEmpty(riseId)) {
                    Profile profile = accountService.getProfileByRiseId(riseId);
                    if (profile != null) {
                        profiles.add(accountService.getProfileByRiseId(riseId));
                    }
                }
                break;
            case "memberId":
                if (!StringUtils.isEmpty(memberId)) {
                    Profile profile = accountService.loadProfileByMemberId(memberId);
                    if (profile != null) {
                        profiles.add(accountService.loadProfileByMemberId(memberId));
                    }
                }
                break;
            default:
                break;
        }

        List<Integer> profileIds = profiles.stream().map(Profile::getId).collect(Collectors.toList());
        List<RiseClassMember> riseClassMembers = monthlyCampService.batchQueryRiseClassMemberByProfileIds(profileIds);
        Map<Integer, RiseClassMember> riseClassMemberMap = riseClassMembers.stream().collect(Collectors.toMap(RiseClassMember::getProfileId, classMember -> classMember));

        List<MonthlyCampDto> monthlyCampDtos = profiles.stream().map(profile -> {
            MonthlyCampDto dto = new MonthlyCampDto();
            dto.setNickName(profile.getNickname());
            dto.setRiseId(profile.getRiseId());
            dto.setHeadImgUrl(profile.getHeadimgurl());

            RiseClassMember riseClassMember = riseClassMemberMap.get(profile.getId());
            if (riseClassMember != null) {
                dto.setRiseClassMemberId(riseClassMember.getId());
                String className = riseClassMember.getClassName();
                if (className != null) {
                    dto.setClassName(className);
                    dto.setClassNameStr(className.substring(0, 2) + "月" + className.substring(2) + "班");
                }
                dto.setGroupId(riseClassMember.getGroupId());
                dto.setMemberId(riseClassMember.getMemberId());
                Integer active = riseClassMember.getActive();
                if (active != null) {
                    dto.setActive(active);
                    dto.setActiveStr(active == 1 ? "学习中" : "已请假");
                }
            }
            return dto;
        }).collect(Collectors.toList());
        return WebUtils.result(monthlyCampDtos);
    }

    @RequestMapping(value = "/load/ungroup", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadUnGroupMonthlyCamp(@ModelAttribute Page page) {
        page.setPageSize(PAGE_SIZE);

        List<RiseClassMember> riseClassMembers = monthlyCampService.loadUnGroupRiseClassMember(page);
        List<Integer> profileIds = riseClassMembers.stream().map(RiseClassMember::getProfileId).collect(Collectors.toList());
        List<Profile> profiles = accountService.getProfiles(profileIds);

        if (riseClassMembers.size() != profiles.size()) {
            return WebUtils.error("人员不匹配");
        }

        MonthlyCampPageDto monthlyCampPageDto = new MonthlyCampPageDto();
        List<MonthlyCampDto> monthlyCampDtos = Lists.newArrayList();
        for (int i = 0; i < riseClassMembers.size(); i++) {
            Integer profileId = riseClassMembers.get(i).getProfileId();
            Profile profile = profiles.stream().filter(item -> profileId.equals(item.getId())).findFirst().orElse(null);
            MonthlyCampDto campDto = convertRiseClassMemberToMonthlyCampDto(riseClassMembers.get(i), profile);
            monthlyCampDtos.add(campDto);
        }
        monthlyCampPageDto.setMonthlyCampDtoList(monthlyCampDtos);
        monthlyCampPageDto.setPage(page);
        return WebUtils.result(monthlyCampPageDto);
    }

    @RequestMapping(value = "/modify/update", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> modifyMonthlyCamp(@RequestBody MonthlyCampDto monthlyCampDto) {
        String RiseId = monthlyCampDto.getRiseId();
        if (RiseId != null) {
            Profile profile = accountService.getProfileByRiseId(RiseId);
            OperationLog operationLog = OperationLog.create()
                    .memo(monthlyCampDto.getTips() + "-" + monthlyCampDto.toString())
                    .openid(profile.getOpenid()).module("小课训练营")
                    .function("信息修改").action("信息修改");
            operationLogService.log(operationLog);
        }

        RiseClassMember riseClassMember = new RiseClassMember();
        riseClassMember.setId(monthlyCampDto.getRiseClassMemberId());
        riseClassMember.setClassName(monthlyCampDto.getClassName());
        riseClassMember.setActive(monthlyCampDto.getActive());
        if (monthlyCampDto.getGroupId() == null || monthlyCampDto.getGroupId().equals("")) {
            riseClassMember.setGroupId(null);
        } else {
            String groupId = monthlyCampDto.getGroupId();
            riseClassMember.setGroupId(groupId.length() == 1 ? "0" + groupId : groupId);
        }
        RiseClassMember updatedRiseClassMember = monthlyCampService.updateRiseClassMemberById(riseClassMember);
        if (updatedRiseClassMember != null) {
            Profile profile = accountService.getProfile(updatedRiseClassMember.getProfileId());
            MonthlyCampDto campDto = convertRiseClassMemberToMonthlyCampDto(updatedRiseClassMember, profile);
            return WebUtils.result(campDto);
        } else {
            return WebUtils.error("更新失败");
        }
    }

    @RequestMapping(value = "/modify/batch/update", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> batchModifyMonthlyCampGroupId(@PathParam("groupId") String groupId, @RequestBody List<Integer> batchRiseClassMemberIds) {
        OperationLog operationLog = OperationLog.create()
                .memo("Ids:" + batchRiseClassMemberIds.toString() + "groupId:" + groupId)
                .openid("").module("小课训练营")
                .function("信息修改").action("批量小组信息修改");
        operationLogService.log(operationLog);

        String formatGroupId = groupId.length() == 1 ? "0" + groupId : groupId;
        Integer result = monthlyCampService.batchUpdateRiseClassMemberByIds(batchRiseClassMemberIds, formatGroupId);
        if (result > 0) {
            return WebUtils.result(result);
        } else {
            return WebUtils.error("更新失败");
        }
    }

    @RequestMapping(value = "/modify/add", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> modifyAddMonthlyCamp(@RequestBody MonthlyCampDto monthlyCampDto) {

        Profile profile = accountService.getProfileByRiseId(monthlyCampDto.getRiseId());
        OperationLog operationLog = OperationLog.create()
                .memo("className:" + monthlyCampDto.getClassName() + ", groupId:" + monthlyCampDto.getGroupId())
                .openid(profile.getOpenid()).module("小课训练营")
                .function("信息新增").action("小课训练营用户新增");
        operationLogService.log(operationLog);

        Integer riseClassMemberId = monthlyCampDto.getRiseClassMemberId();
        RiseClassMember riseClassMember = monthlyCampService.loadRiseClassMemberById(riseClassMemberId);
        if (riseClassMember == null) {
            riseClassMember = new RiseClassMember();
            riseClassMember.setClassId(monthlyCampDto.getClassName());
            riseClassMember.setClassName(monthlyCampDto.getClassName());
            String memberId = signupService.generateMemberId();
            riseClassMember.setMemberId(memberId);
            riseClassMember.setGroupId(monthlyCampDto.getGroupId());
            riseClassMember.setProfileId(profile.getId());

            MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
            riseClassMember.setYear(monthlyCampConfig.getSellingYear());
            riseClassMember.setMonth(monthlyCampConfig.getSellingMonth());

            riseClassMember.setActive(monthlyCampDto.getActive());
            int result = monthlyCampService.initRiseClassMember(riseClassMember);
            if (result > 0) {
                riseClassMember.setId(result);
                MonthlyCampDto campDto = convertRiseClassMemberToMonthlyCampDto(riseClassMember, profile);
                return WebUtils.result(campDto);
            } else {
                return WebUtils.error("用户新增失败，请及时联系管理员");
            }
        } else {
            return WebUtils.error("当前用户已经是小课训练营用户");
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
            String month = String.format("%02d", Integer.parseInt(className.substring(0, 2)));
            String classStr = String.format("%02d", Integer.parseInt(className.substring(2)));
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
