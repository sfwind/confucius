package com.iquanwai.confucius.web.pc.backend.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.backend.MonthlyCampService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.util.ThreadPool;
import com.iquanwai.confucius.web.pc.backend.dto.CampRiseCertificateDao;
import com.iquanwai.confucius.web.pc.backend.dto.MonthlyCampDto;
import com.iquanwai.confucius.web.pc.backend.dto.MonthlyCampDtoGroup;
import com.iquanwai.confucius.web.pc.backend.dto.MonthlyCampProcessDto;
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
    private AccountService accountService;

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
    public ResponseEntity<Map<String, Object>> loadProfile(@PathVariable("type") String type, @PathParam("nickName") String nickName,
                                                           @PathParam("riseId") String riseId, @PathParam("memberId") String memberId) {
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

        Map<Integer, Profile> profileMap = profiles.stream().collect(Collectors.toMap(Profile::getId, profile -> profile));
        List<Integer> profileIds = profiles.stream().map(Profile::getId).collect(Collectors.toList());
        List<RiseClassMember> riseClassMembers = monthlyCampService.batchQueryRiseClassMemberByProfileIds(profileIds);

        List<MonthlyCampDto> monthlyCampDtos = riseClassMembers.stream().map(riseClassMember -> {
            MonthlyCampDto dto = new MonthlyCampDto();
            Profile profile = profileMap.get(riseClassMember.getProfileId());
            if (profile != null) {
                dto.setNickName(profile.getNickname());
                dto.setRiseId(profile.getRiseId());
                dto.setHeadImgUrl(profile.getHeadimgurl());
                dto.setMemberId(profile.getMemberId());
            }

            dto.setRiseClassMemberId(riseClassMember.getId());
            String className = riseClassMember.getClassName();
            if (className != null) {
                dto.setClassName(className);
                dto.setClassNameStr(className.substring(0, 2) + "月" + className.substring(2) + "班");
            }
            dto.setGroupId(riseClassMember.getGroupId());
            Integer active = riseClassMember.getActive();
            if (active != null) {
                dto.setActive(active);
                dto.setActiveStr(active == 1 ? "学习中" : "已请假");
            }
            return dto;
        }).collect(Collectors.toList());

        monthlyCampDtos.addAll(profiles.stream().map(profile -> {
            MonthlyCampDto dto = new MonthlyCampDto();
            dto.setNickName(profile.getNickname());
            dto.setRiseId(profile.getRiseId());
            dto.setHeadImgUrl(profile.getHeadimgurl());
            return dto;
        }).collect(Collectors.toList()));

        return WebUtils.result(monthlyCampDtos);
    }

    @RequestMapping(value = "/switch")
    public ResponseEntity<Map<String, Object>> switchCampAndProcessData(@RequestBody MonthlyCampProcessDto campProcessDto) {
        Integer sourceYear = campProcessDto.getSourceYear();
        Integer sourceMonth = campProcessDto.getSourceMonth();
        Integer targetYear = campProcessDto.getTargetYear();
        Integer targetMonth = campProcessDto.getTargetMonth();

        ThreadPool.execute(() -> monthlyCampService.switchCampDataProcess(sourceYear, sourceMonth, targetYear, targetMonth));
        return WebUtils.success();
    }

    @RequestMapping(value = "/add/certificate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addCampRiseCertificate(@RequestBody CampRiseCertificateDao campRiseCertificateDao) {
        Integer type = campRiseCertificateDao.getType();
        Integer year = campRiseCertificateDao.getYear();
        Integer month = campRiseCertificateDao.getMonth();
        List<String> memberIds = campRiseCertificateDao.getMemberIds();
        logger.info("开始添加获得证书人员");
        monthlyCampService.insertRiseCertificate(year, month, type, memberIds);
        logger.info("证书人员添加结束");
        return WebUtils.success();
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
        campDto.setMemberId(profile.getMemberId());
        campDto.setActive(riseClassMember.getActive());
        campDto.setActiveStr(riseClassMember.getActive() == 1 ? "学习中" : "已请假");

        return campDto;
    }

}
