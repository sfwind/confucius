package com.iquanwai.confucius.web.pc.backend.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.asst.AssistantCoachService;
import com.iquanwai.confucius.biz.domain.asst.AsstUpService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.TableDto;
import com.iquanwai.confucius.biz.po.asst.AsstUpDefault;
import com.iquanwai.confucius.biz.po.asst.AsstUpExecution;
import com.iquanwai.confucius.biz.po.asst.AsstUpStandard;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.PracticePlan;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.enums.AssistCatalogEnums;
import com.iquanwai.confucius.web.pc.backend.dto.*;
import com.iquanwai.confucius.web.pc.datahelper.AsstHelper;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
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
    @Autowired
    private PlanService planService;

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
                Integer  profileId = profile.getId();
                AsstUpStandard asstUpStandard = asstUpService.loadStandard(profileId);
                AsstUpExecution asstUpExecution = asstUpService.loadUpGradeExecution(profileId);
                if (checkIsReached(profileId, asstUpStandard, asstUpExecution)) {
                    assistDto.setReached("是");
                } else {
                    assistDto.setReached("否");
                }
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
    @RequestMapping("update")
    public ResponseEntity<Map<String, Object>> updateAssist(PCLoginUser loginUser, @RequestParam("riseId") String riseId, @RequestParam("assist") Integer assistId, @RequestParam("catalog") Integer roleId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("教练管理").function("教练升降级").action("教练升降级");
        operationLogService.log(operationLog);

        Profile profile = accountService.getProfileByRiseId(riseId);
        if (profile == null) {
            return WebUtils.error("该用户不存在");
        }

        if (AssistCatalogEnums.EXPIRED_ASSIST.getRoleId().equals(roleId)) {
            if (assistantCoachService.deleteAssist(assistId) == -1) {
                return WebUtils.error("更新教练级别失败");
            }
        } else {
            if (assistantCoachService.updateAssist(assistId, roleId) == -1) {
                return WebUtils.error("更新教练级别失败");
            }
            Integer profileId = profile.getId();
            AsstUpDefault asstUpDefault = asstUpService.loadDefaultByRoleId(roleId);
            if (asstUpDefault == null) {
                return WebUtils.error("没有对应的助教默认表");
            }
            AsstUpStandard asstUpStandard = new AsstUpStandard();
            BeanUtils.copyProperties(asstUpDefault, asstUpStandard);
            asstUpStandard.setProfileId(profileId);
            asstUpStandard.setRoleId(roleId);
            Integer standardId = asstUpService.insertStandard(asstUpStandard);
            if (standardId == -1) {
                return WebUtils.error("生成助教标准表失败");
            }
            Date date = DateUtils.afterDays(new Date(), 0);
            if (asstUpService.insertExecution(standardId, profileId, roleId, date) == -1) {
                return WebUtils.error("生成助教完成度表失败");
            }
        }
        return WebUtils.success();
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
    public ResponseEntity<Map<String, Object>> addAssist(PCLoginUser loginUser, @PathVariable String riseId, @PathVariable("assistCatalog") Integer roleId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("教练管理").function("添加教练").action("添加教练");
        operationLogService.log(operationLog);

        Profile profile = accountService.getProfileByRiseId(riseId);
        if (profile == null) {
            return WebUtils.error("该用户不存在");
        }
        assistantCoachService.addAssist(roleId, riseId);

        Integer profileId = profile.getId();
        AsstUpDefault asstUpDefault = asstUpService.loadDefaultByRoleId(roleId);
        if (asstUpDefault == null) {
            return WebUtils.error("没有对应的助教默认表");
        }
        AsstUpStandard asstUpStandard = new AsstUpStandard();
        BeanUtils.copyProperties(asstUpDefault, asstUpStandard);
        asstUpStandard.setProfileId(profileId);
        asstUpStandard.setRoleId(roleId);
        Integer standardId = asstUpService.insertStandard(asstUpStandard);
        if (standardId == -1) {
            return WebUtils.error("生成助教标准表失败");
        }
        Date date = DateUtils.afterDays(new Date(), 0);
        if (asstUpService.insertExecution(standardId, profileId, roleId, date) == -1) {
            return WebUtils.error("生成助教完成度表失败");
        }
        return WebUtils.success();

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

    @RequestMapping(value = "/standard/update", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateAssistStandard(PCLoginUser loginUser, @RequestBody AsstStandardDto asstStandardDto) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理").function("助教管理").action("更新教练实际标准");

        operationLogService.log(operationLog);

        AsstUpStandard asstUpStandard = new AsstUpStandard();
        BeanUtils.copyProperties(asstStandardDto, asstUpStandard);

        if (asstUpService.updateStandard(asstUpStandard) >= 0) {
            return WebUtils.success();
        }
        return WebUtils.error("更新失败");
    }

    @RequestMapping("/default/load")
    public ResponseEntity<Map<String, Object>> loadAssistDefault(PCLoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理").function("助教管理").action("加载助教默认标准");
        operationLogService.log(operationLog);

        return WebUtils.result(asstUpService.loadAssistDefault());
    }


    @RequestMapping("/execution/load")
    public ResponseEntity<Map<String, Object>> loadAssistsExecution(@ModelAttribute Page page, PCLoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理").function("助教管理").action("加载助教完成情况");
        operationLogService.log(operationLog);

        if (page == null) {
            page = new Page();
        }
        page.setPageSize(20);
        List<UserRole> userRoles = asstUpService.loadAssists(page);
        TableDto<GradeDto> result = new TableDto<>();
        result.setPage(page);
        result.setData(initExecutions(userRoles));
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/execution/update", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateAssistExecution(PCLoginUser loginUser, @RequestBody AsstExecutionDto asstExecutionDto) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理").function("助教管理").action("更新助教完成情况");

        operationLogService.log(operationLog);

        AsstUpExecution asstUpExecution = new AsstUpExecution();
        BeanUtils.copyProperties(asstExecutionDto, asstUpExecution);
        //增量更新
        asstUpExecution = genUpdateExecution(asstUpExecution);
        if (asstUpExecution == null) {
            return WebUtils.error("助教完成情况数据异常");
        }
        if (asstUpService.updateExecution(asstUpExecution) >= 0) {
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
            if (profile == null) {
                return;
            }
            asstStandardDto.setNickName(profile.getNickname());
            Integer roleId = userRole.getRoleId();
            asstStandardDto.setRoleName(AssistCatalogEnums.getById(roleId).getRoleName());
            AsstUpStandard asstStandard = asstUpService.loadStandard(profile.getId());
            BeanUtils.copyProperties(asstStandard, asstStandardDto);
            asstStandardDtos.add(asstStandardDto);
        });
        return asstStandardDtos;
    }

//    private List<AsstExecutionDto> initExecutions(List<UserRole> userRoles) {
//        List<AsstExecutionDto> asstExecutionDtos = Lists.newArrayList();
//        userRoles.forEach(userRole -> {
//            AsstExecutionDto asstExecutionDto = new AsstExecutionDto();
//
//            Profile profile = accountService.getProfile(userRole.getProfileId());
//            if (profile == null) {
//                return;
//            }
//            Integer profileId = profile.getId();
//            asstExecutionDto.setNickName(profile.getNickname());
//            Integer roleId = userRole.getRoleId();
//            AsstUpStandard asstUpStandard = asstUpService.loadStandard(profileId);
//            if (asstUpStandard == null) {
//                return;
//            }
//            asstExecutionDto.setCountDown(asstUpStandard.getCountDown());
//            asstExecutionDto.setRoleName(AssistCatalogEnums.getById(roleId).getRoleName());
//            Long result = planService.getUserPlans(profileId).stream().filter(improvementPlan -> improvementPlan.getCompleteTime()!=null).count();
//            asstExecutionDto.setLearnedProblem(result.intValue());
//            AsstUpExecution asstUpExecution = asstUpService.loadUpGradeExecution(profile.getId());
//            BeanUtils.copyProperties(asstUpExecution, asstExecutionDto);
//            if (asstExecutionDto.getReviewNumber() == 0) {
//                asstExecutionDto.setValidReviewRate(0);
//            } else {
//                asstExecutionDto.setValidReviewRate((asstExecutionDto.getValidReviewNumber() * 100) / asstExecutionDto.getReviewNumber());
//            }
//            asstExecutionDtos.add(asstExecutionDto);
//        });
//        return asstExecutionDtos;
//    }


    private List<GradeDto> initExecutions(List<UserRole> userRoles) {
        List<GradeDto> gradeDtos = Lists.newArrayList();
        userRoles.forEach(userRole -> {
            GradeDto upGradeDto = new GradeDto();

            Profile profile = accountService.getProfile(userRole.getProfileId());
            if (profile == null) {
                return;
            }
            Integer profileId = profile.getId();
            upGradeDto.setNickName(profile.getNickname());
            Integer roleId = userRole.getRoleId();
            AsstUpStandard asstUpStandard = asstUpService.loadStandard(profileId);
            if (asstUpStandard == null) {
                return;
            }
            AsstUpExecution asstUpExecution = asstUpService.loadUpGradeExecution(profileId);
            if (asstUpExecution == null) {
                return;
            }

            upGradeDto.setId(asstUpExecution.getId());
            upGradeDto.setRoleName(AssistCatalogEnums.getById(roleId).getRoleName());
            Integer interval = DateUtils.interval(asstUpExecution.getStartDate());
            Integer countDown = asstUpStandard.getCountDown();
            upGradeDto.setStartDate(asstUpExecution.getStartDate());
            upGradeDto.setCountDown(countDown);
            upGradeDto.setRemainDay(getRemain(interval, countDown));
            Integer applicationRate = asstUpStandard.getApplicationRate();
            //统计完成度在applicationRate之上的课程数量
           Integer finish = planService.getUserPlans(profileId).stream().filter(improvementPlan -> improvementPlan.getCompleteTime()!=null).map(improvementPlan -> {
                List<PracticePlan> practicePlans = planService.loadPracticePlans(improvementPlan.getId());
                Long sum = practicePlans.stream().filter(practicePlan -> (practicePlan.getType() == PracticePlan.APPLICATION) || (practicePlan.getType()==PracticePlan.APPLICATION_REVIEW)).count();
                Long count = practicePlans.stream().filter(practicePlan ->(practicePlan.getStatus()==1)&& (practicePlan.getType() == PracticePlan.APPLICATION) || (practicePlan.getType()==PracticePlan.APPLICATION_REVIEW)).count();
               if(count*100/sum>=applicationRate){
                   return 1;
               }else{
                   return 0;
               }
           }).reduce(0,Integer::sum);

            Integer total = asstUpStandard.getLearnedProblem();
            upGradeDto.setNeedLearnedProblem(total);
            upGradeDto.setLearnedProblem(finish);
            upGradeDto.setRemainProblem(getRemain(finish, total));
            finish = asstUpExecution.getReviewNumber();
            total = asstUpStandard.getReviewNumber();
            upGradeDto.setNeedReviewedNumber(total);
            upGradeDto.setReviewedNumber(finish);
            upGradeDto.setRemainReviewNumber(getRemain(finish, total));
            Integer valid = asstUpExecution.getValidReviewNumber();
            total = asstUpStandard.getRequestReviewNumber();
            finish = asstUpExecution.getReviewNumber();
            upGradeDto.setNeedRequestReviewNumber(total);
            upGradeDto.setRequestReviewNumber(finish);
            upGradeDto.setRemainRequestReviewNumber(getRemain(finish, total));
            upGradeDto.setNeedReviewRate(asstUpStandard.getValidReviewRate());
            if (finish == 0) {
                upGradeDto.setReviewRate(0);
            } else {
                upGradeDto.setReviewRate(valid * 100 / finish);
            }
            finish = asstUpExecution.getHighQualityAnswer();
            total = asstUpStandard.getHighQualityAnswer();
            upGradeDto.setNeedHighAnswer(total);
            upGradeDto.setHighAnswer(finish);
            upGradeDto.setRemainHighAnswer(getRemain(finish, total));

            total = asstUpStandard.getHostNumber();
            finish = asstUpExecution.getHostNumber();

            upGradeDto.setNeedHostNumber(total);
            upGradeDto.setHostNumber(finish);
            upGradeDto.setRemainHostNumber(getRemain(finish, total));

            upGradeDto.setNeedHostScore(asstUpStandard.getHostScore());
            upGradeDto.setHostScore(asstUpExecution.getHostScore());

            total = asstUpStandard.getMainPointNumber();
            finish = asstUpExecution.getMainPointNumber();
            upGradeDto.setNeedMainPointNumber(total);
            upGradeDto.setMainPointNumber(finish);
            upGradeDto.setRemainPointNumber(getRemain(finish, total));

            upGradeDto.setNeedPointScore(asstUpStandard.getMainPointScore());
            upGradeDto.setMainPointScore(asstUpExecution.getMainPointScore());
            upGradeDto.setNeedOnlineAnswer(asstUpStandard.getOnlineAnswer());
            upGradeDto.setOnlineAnswer(asstUpExecution.getOnlineAnswer());
            upGradeDto.setNeedSwing(asstUpStandard.getSwing());
            upGradeDto.setSwing(asstUpExecution.getSwing());

            total = asstUpStandard.getOnlineOrSwingNumber();
            finish = asstUpExecution.getOnlineOrSwingNumber();
            upGradeDto.setNeedOnlineNumber(total);
            upGradeDto.setOnlineOrSwingNumber(finish);
            upGradeDto.setRemainOnlineOrSwingNumber(getRemain(finish, total));
            upGradeDto.setNeedOnlineScore(asstUpStandard.getOnlineScore());
            upGradeDto.setOnlineScore(asstUpExecution.getOnlineScore());
            total = asstUpStandard.getCampNumber();
            finish = asstUpExecution.getCampNumber();
            upGradeDto.setNeedCampNumber(total);
            upGradeDto.setCampNumber(finish);
            upGradeDto.setRemainCampNumber(getRemain(finish, total));
            total = asstUpStandard.getAsstNumber();
            finish = asstUpExecution.getAsstNumber();
            upGradeDto.setNeedAsstNumber(total);
            upGradeDto.setAsstNumber(finish);
            upGradeDto.setRemainAsstNumber(getRemain(finish, total));
            upGradeDto.setNeedCampScore(asstUpStandard.getCampScore());
            upGradeDto.setCampScore(asstUpExecution.getCampScore());
            upGradeDto.setNeedMonthlyWork(asstUpStandard.getMonthlyWork());
            upGradeDto.setMonthlyWork(asstUpExecution.getMonthlyWork());

            total = asstUpStandard.getFosterNew();
            finish = asstUpExecution.getFosterNew();
            upGradeDto.setNeedFosterNew(total);
            upGradeDto.setFosterNew(finish);
            upGradeDto.setRemainFosterNew(getRemain(finish, total));

            total = asstUpStandard.getCompanyTrainNumber();
            finish = asstUpExecution.getCompanyTrainNumber();
            upGradeDto.setNeedCompanyNumber(total);
            upGradeDto.setCompanyNumber(finish);
            upGradeDto.setRemainCompanyNumber(getRemain(finish, total));
            upGradeDto.setNeedCompanyScore(asstUpStandard.getCompanyTrainScore());
            upGradeDto.setCompanyScore(asstUpExecution.getCompanyTrainScore());

            gradeDtos.add(upGradeDto);
        });

        return gradeDtos;
    }


    private AsstUpExecution genUpdateExecution(AsstUpExecution asstUpExecution) {
        AsstUpExecution existExecution = asstUpService.load(asstUpExecution.getId());
        if (existExecution == null) {
            return null;
        }
        asstUpExecution.setReviewNumber(existExecution.getReviewNumber() + asstUpExecution.getReviewNumber());
        asstUpExecution.setRequestReviewNumber(existExecution.getRequestReviewNumber() + asstUpExecution.getRequestReviewNumber());
        asstUpExecution.setValidReviewNumber(existExecution.getValidReviewNumber() + asstUpExecution.getValidReviewNumber());
        asstUpExecution.setHighQualityAnswer(existExecution.getHighQualityAnswer() + asstUpExecution.getHighQualityAnswer());
        asstUpExecution.setHostNumber(existExecution.getHostNumber() + asstUpExecution.getHostNumber());
        asstUpExecution.setHostScore(asstUpExecution.getHostScore());
        asstUpExecution.setMainPointNumber(existExecution.getMainPointNumber() + asstUpExecution.getMainPointNumber());
        asstUpExecution.setMainPointScore(asstUpExecution.getMainPointScore());
        asstUpExecution.setOnlineOrSwingNumber(existExecution.getOnlineOrSwingNumber() + asstUpExecution.getOnlineOrSwingNumber());
        asstUpExecution.setOnlineScore(asstUpExecution.getOnlineScore());
        asstUpExecution.setCampNumber(existExecution.getCampNumber() + asstUpExecution.getCampNumber());
        asstUpExecution.setAsstNumber(existExecution.getAsstNumber() + asstUpExecution.getAsstNumber());
        asstUpExecution.setCampScore(asstUpExecution.getCampScore());
        asstUpExecution.setFosterNew(existExecution.getFosterNew() + asstUpExecution.getFosterNew());
        asstUpExecution.setCompanyTrainNumber(existExecution.getCompanyTrainNumber() + asstUpExecution.getCompanyTrainNumber());
        asstUpExecution.setCompanyTrainScore(asstUpExecution.getCompanyTrainScore());
        return asstUpExecution;
    }

    /**
     * 判断是否达标
     *
     * @param profileId
     * @param asstUpStandard
     * @param asstUpExecution
     * @return
     */
    private boolean checkIsReached(Integer profileId, AsstUpStandard asstUpStandard, AsstUpExecution asstUpExecution) {
        Long result = planService.getUserPlans(profileId).stream().filter(improvementPlan -> improvementPlan.getCompleteTime() != null).count();
        if (asstUpStandard.getLearnedProblem() > result.intValue()) {
            return false;
        }
       return   AsstHelper.checkIsReached(asstUpStandard,asstUpExecution);
    }


    /**
     * 计算剩余需要的完成数（如果已经完成的大于所有的，则返回0）
     *
     * @param finished
     * @param totalNum
     * @return
     */
    private Integer getRemain(Integer finished, Integer totalNum) {
        if (totalNum < finished) {
            return 0;
        } else {
            return totalNum - finished;
        }
    }

}
