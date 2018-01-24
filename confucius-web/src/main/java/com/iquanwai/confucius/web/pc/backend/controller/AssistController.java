package com.iquanwai.confucius.web.pc.backend.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.asst.AssistantCoachService;
import com.iquanwai.confucius.biz.domain.asst.AsstUpService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.TableDto;
import com.iquanwai.confucius.biz.po.asst.AsstUpStandard;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.enums.AssistCatalogEnums;
import com.iquanwai.confucius.web.pc.backend.dto.AssistCatalogDto;
import com.iquanwai.confucius.web.pc.backend.dto.AssistDto;
import com.iquanwai.confucius.web.pc.backend.dto.AsstStandardDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private AsstUpService asstUpService;

    /**
     * 加载所有的教练
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

            assistDto.setRoleName(AssistCatalogEnums.getById(userRole.getRoleId()).getRoleName());

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
     */
    @RequestMapping("update/{assistId}/{assistCatalog}")
    public ResponseEntity<Map<String, Object>> updateAssist(PCLoginUser loginUser, @PathVariable Integer assistId, @PathVariable Integer assistCatalog) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("教练管理").function("教练升降级").action("教练升降级");
        operationLogService.log(operationLog);

        if (assistCatalog.equals(AssistCatalogEnums.EXPIRED_ASSIST.getRoleId())) {
            return WebUtils.result(assistantCoachService.deleteAssist(assistId));
        } else {
            return WebUtils.result(assistantCoachService.updateAssist(assistId, assistCatalog));
        }
    }


    /**
     * 根据NickName加载非教练人员
     */
    @RequestMapping("load/unassist/{nickName}")
    public ResponseEntity<Map<String, Object>> loadUnAssistByNickName(PCLoginUser loginUser, @PathVariable String nickName) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("教练管理").function("加载非教练人员").action("加载非教练人员");
        operationLogService.log(operationLog);

        List<Profile> profiles = assistantCoachService.loadUnAssistByNickName(nickName);

        List<AssistDto> assistDtos = Lists.newArrayList();

        profiles.stream().forEach(profile -> {
            AssistDto assistDto = new AssistDto();
            assistDto.setId(-1);
            assistDto.setHeadImageUrl(profile.getHeadimgurl());
            assistDto.setRoleId(11);
            assistDto.setRiseId(profile.getRiseId());
            assistDto.setNickName(profile.getNickname());

            assistDtos.add(assistDto);
        });

        return WebUtils.result(assistDtos);
    }

    @RequestMapping("add/{riseId}/{assistCatalog}")
    public ResponseEntity<Map<String, Object>> addAssist(PCLoginUser loginUser, @PathVariable String riseId, @PathVariable Integer assistCatalog) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("教练管理").function("添加教练").action("添加教练");
        operationLogService.log(operationLog);

        return WebUtils.result(assistantCoachService.addAssist(assistCatalog, riseId));
    }

    /**
     * 加载助教评判标准
     *
     * @param loginUser
     * @return
     */
    @RequestMapping("/standard/load")
    public ResponseEntity<Map<String, Object>> loadAssistStandard(@ModelAttribute Page page, PCLoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("后台管理").function("教练管理").action("加载评判标准");
        operationLogService.log(operationLog);
        if (page == null) {
            page = new Page();
        }
        page.setPageSize(20);
        List<UserRole> userRoles = asstUpService.loadAssists(page);
        TableDto<AsstStandardDto> result = new TableDto<>();
        result.setPage(page);
        result.setData(initStandards(userRoles));
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/standard/update",method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> updateAssistStandard(PCLoginUser loginUser,@RequestBody AsstStandardDto asstStandardDto){
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理").function("助教管理").action("更新教练实际标准").memo(asstStandardDto.getProfileId().toString());

        operationLogService.log(operationLog);

        Profile profile = accountService.getProfile(asstStandardDto.getProfileId());
        if(profile == null){
            return WebUtils.error("用户不存在");
        }
        AsstUpStandard asstUpStandard = new AsstUpStandard();
        BeanUtils.copyProperties(asstStandardDto,asstUpStandard);

        if(asstUpService.updateStandard(asstUpStandard)>=0){
            return WebUtils.success();
        }
        return WebUtils.error("更新失败");
    }


    /**
     * 生成评判标准
     *
     * @param userRoles
     * @return
     */
    private List<AsstStandardDto> initStandards(List<UserRole> userRoles) {
        List<AsstStandardDto> asstStandardDtos = Lists.newArrayList();
        userRoles.forEach(userRole -> {
            AsstStandardDto asstStandardDto = new AsstStandardDto();

            Profile profile = accountService.getProfile(userRole.getProfileId());
            if(profile == null){
                return;
            }
            asstStandardDto.setNickName(profile.getNickname());
            Integer roleId = userRole.getRoleId();
            asstStandardDto.setRoleName(AssistCatalogEnums.getById(roleId).getRoleName());
            AsstUpStandard asstStandard = asstUpService.loadStandard(profile.getId());
            BeanUtils.copyProperties(asstStandard,asstStandardDto);
            asstStandardDtos.add(asstStandardDto);
        });
        return asstStandardDtos;
    }
}
