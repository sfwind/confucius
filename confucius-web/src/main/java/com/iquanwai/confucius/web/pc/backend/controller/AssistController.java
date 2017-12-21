package com.iquanwai.confucius.web.pc.backend.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.asst.AssistantCoachService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.web.enums.AssistCatalogEnums;
import com.iquanwai.confucius.web.pc.backend.dto.AssistCatalogDto;
import com.iquanwai.confucius.web.pc.backend.dto.AssistDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 助教后台管理
 */
@RestController
@RequestMapping("pc/operation/assist")
public class AssistController {


    @Autowired
    private AssistantCoachService assistantCoachService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AccountService accountService;

    /**
     * 加载所有的教练
     *
     * @param loginUser
     * @return
     */
    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> loadAssists(PCLoginUser loginUser) {

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("教练管理").function("加载教练").action("加载所有教练");
        operationLogService.log(operationLog);

        List<UserRole> userRoleList = assistantCoachService.loadAssists();
        List<AssistDto> assistDtoList = Lists.newArrayList();
        userRoleList.forEach(userRole -> {
            AssistDto assistDto = new AssistDto();

            BeanUtils.copyProperties(userRole, assistDto);

            if (userRole.getRoleId().equals(AssistCatalogEnums.PROBATIONARY_ASSIST.getRoleId())) {
                assistDto.setRoleName(AssistCatalogEnums.PROBATIONARY_ASSIST.getRoleName());
            } else if (userRole.getRoleId().equals(AssistCatalogEnums.ASSIST.getRoleId())) {
                assistDto.setRoleName(AssistCatalogEnums.ASSIST.getRoleName());
            } else if (userRole.getRoleId().equals(AssistCatalogEnums.CANDIDATE_ASSIST.getRoleId())) {
                assistDto.setRoleName(AssistCatalogEnums.CANDIDATE_ASSIST.getRoleName());
            } else {
                assistDto.setRoleName("未知");
            }

            Profile profile = accountService.getProfile(userRole.getProfileId());
            if (profile != null) {
                assistDto.setNickName(profile.getNickname());
                assistDto.setRiseId(profile.getRiseId());
                assistDto.setHeadImageUrl(profile.getHeadimgurl());
            }
            assistDtoList.add(assistDto);
        });
        return WebUtils.result(assistDtoList);
    }


    /**
     * 加载教练类别
     *
     * @param loginUser
     * @return
     */
    @RequestMapping("/load/catalog")
    public ResponseEntity<Map<String, Object>> loadAssistCatalogs(PCLoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("教练管理").function("加载教练类别").action("加载所有教练类别");
        operationLogService.log(operationLog);
        List<AssistCatalogDto> assistCatalogDtoList = Lists.newArrayList();
        for (AssistCatalogEnums e : AssistCatalogEnums.values()) {
            AssistCatalogDto assistCatalogDto = new AssistCatalogDto();
            assistCatalogDto.setCatalogId(e.getRoleId());
            assistCatalogDto.setCatalogName(e.getRoleName());
            assistCatalogDtoList.add(assistCatalogDto);
        }
        return WebUtils.result(assistCatalogDtoList);
    }


    /**
     * 修改教练状态
     * @param loginUser
     * @param assistId
     * @param assistCatalog
     * @return
     */
    @RequestMapping("update/{assistId}/{assistCatalog}")
    public ResponseEntity<Map<String,Object>> updateAssist(PCLoginUser loginUser, @PathVariable Integer assistId,@PathVariable Integer assistCatalog){
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("教练管理").function("教练升降级").action("教练升降级");
            operationLogService.log(operationLog);

            if(assistCatalog.equals(AssistCatalogEnums.EXPIRED_ASSIST.getRoleId())){
                return WebUtils.result(assistantCoachService.deleteAssist(assistId));
            } else {
                return WebUtils.result(assistantCoachService.updateAssist(assistId,assistCatalog));
            }
    }


}
