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
import com.iquanwai.confucius.biz.po.fragmentation.PracticePlan;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.enums.AssistCatalogEnums;
import com.iquanwai.confucius.web.pc.asst.dto.UpGradeDto;
import com.iquanwai.confucius.web.pc.backend.dto.*;
import com.iquanwai.confucius.web.pc.datahelper.AsstHelper;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
                Integer profileId = profile.getId();
                AsstUpStandard asstUpStandard = asstUpService.loadStandard(profileId);
                AsstUpExecution asstUpExecution = asstUpService.loadUpGradeExecution(profileId);
                if ((asstUpStandard != null) && (asstUpExecution != null)) {
                    assistDto.setRemainCount(unReachedCount(profileId, asstUpStandard, asstUpExecution));
                    if (checkIsReached(profileId, asstUpStandard, asstUpExecution)) {
                        assistDto.setReached("是");
                    } else {
                        assistDto.setReached("否");
                    }
                    Integer interval = DateUtils.interval(asstUpExecution.getStartDate());
                    Integer countDown = asstUpStandard.getCountDown();
                    assistDto.setRemainDay(AsstHelper.getRemain(interval, countDown));
                    assistDto.setNeedVerified(asstUpStandard.getNeedVerified());
                    assistDto.setUpGrade(asstUpExecution.getUpGrade());

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
    @RequestMapping("/standard/search/load")
    public ResponseEntity<Map<String, Object>> loadSearchStandard(@ModelAttribute Page page, PCLoginUser loginUser, @RequestParam("riseId") String riseId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理").function("助教管理").action("查询标准情况");
        operationLogService.log(operationLog);

        List<Integer> profiles = searchProfiles(riseId);
        if (profiles.size() == 0) {
            return WebUtils.error("没有该用户");
        }
        List<UserRole> userRoles = asstUpService.loadSearchAssists(profiles);
        if (userRoles.size() == 0) {
            return WebUtils.error("不存在该助教");
        }
        return WebUtils.result(initStandards(userRoles));
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

    @RequestMapping("/execution/search/load")
    public ResponseEntity<Map<String, Object>> loadSearchExecution(PCLoginUser loginUser, @RequestParam("riseId") String riseId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理").function("助教管理").action("查询完成情况");
        operationLogService.log(operationLog);
        //根据昵称和riseId进行匹配
        List<Integer> profiles = searchProfiles(riseId);
        if (profiles.size() == 0) {
            return WebUtils.error("没有该用户");
        }
        List<UserRole> userRoles = asstUpService.loadSearchAssists(profiles);
        if (userRoles.size() == 0) {
            return WebUtils.error("不存在该助教");
        }
        return WebUtils.result(initExecutions(userRoles));
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

    @RequestMapping(value = "/execution/file/update", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateAssistExecution(PCLoginUser loginUser, @RequestParam(value = "file") MultipartFile excelFile) {

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理").function("助教管理").action("更新助教完成情况");

        operationLogService.log(operationLog);

        if (asstUpService.updateExecution(excelFile) == -1) {
            return WebUtils.error("导入助教完成数据出错");
        }
        return WebUtils.success();
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
            AssistCatalogEnums assistCatalogEnums = AssistCatalogEnums.getById(roleId);
            if (assistCatalogEnums == null) {
                return;
            }
            asstStandardDto.setRoleName(assistCatalogEnums.getRoleName());
            AsstUpStandard asstStandard = asstUpService.loadStandard(profile.getId());
            if (asstStandard == null) {
                return;
            }
            BeanUtils.copyProperties(asstStandard, asstStandardDto);
            asstStandardDtos.add(asstStandardDto);
        });
        return asstStandardDtos;
    }

    private List<GradeDto> initExecutions(List<UserRole> userRoles) {
        List<GradeDto> gradeDtos = Lists.newArrayList();
        userRoles.forEach(userRole -> {
            GradeDto gradeDto = new GradeDto();

            Profile profile = accountService.getProfile(userRole.getProfileId());
            if (profile == null) {
                return;
            }
            Integer profileId = profile.getId();
            gradeDto.setNickName(profile.getNickname());
            Integer roleId = userRole.getRoleId();
            AsstUpStandard asstUpStandard = asstUpService.loadStandard(profileId);
            if (asstUpStandard == null) {
                return;
            }
            AsstUpExecution asstUpExecution = asstUpService.loadUpGradeExecution(profileId);
            if (asstUpExecution == null) {
                return;
            }

            UpGradeDto upGradeDto = AsstHelper.genUpGradeInfo(asstUpStandard, asstUpExecution);

            BeanUtils.copyProperties(upGradeDto, gradeDto);

            gradeDto.setId(asstUpExecution.getId());
            AssistCatalogEnums assistCatalogEnums = AssistCatalogEnums.getById(roleId);
            if (assistCatalogEnums == null) {
                return;
            }
            gradeDto.setRoleName(assistCatalogEnums.getRoleName());

            Integer applicationRate = asstUpStandard.getApplicationRate();
            //统计完成度在applicationRate之上的课程数量
            Integer finish = planService.getUserPlans(profileId).stream().filter(improvementPlan -> improvementPlan.getCompleteTime() != null).map(improvementPlan -> {
                List<PracticePlan> practicePlans = planService.loadPracticePlans(improvementPlan.getId());
                Long sum = practicePlans.stream().filter(practicePlan -> (practicePlan.getType() == PracticePlan.APPLICATION) || (practicePlan.getType() == PracticePlan.APPLICATION_REVIEW)).count();
                Long count = practicePlans.stream().filter(practicePlan -> (practicePlan.getStatus() == 1) && (practicePlan.getType() == PracticePlan.APPLICATION) || (practicePlan.getType() == PracticePlan.APPLICATION_REVIEW)).count();

                if (count * 100 / sum >= applicationRate) {
                    return 1;
                } else {
                    return 0;
                }
            }).reduce(0, Integer::sum);

            Integer total = asstUpStandard.getLearnedProblem();
            gradeDto.setNeedLearnedProblem(total);
            gradeDto.setLearnedProblem(finish);
            gradeDto.setRemainProblem(AsstHelper.getRemain(finish, total));


            gradeDtos.add(gradeDto);
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
        asstUpExecution.setHostNumber(asstUpExecution.getHostNumber());
        asstUpExecution.setHostScore(asstUpExecution.getHostScore());
        asstUpExecution.setMainPointNumber(asstUpExecution.getMainPointNumber());
        asstUpExecution.setMainPointScore(asstUpExecution.getMainPointScore());
        asstUpExecution.setOnlineOrSwingNumber(asstUpExecution.getOnlineOrSwingNumber());
        asstUpExecution.setOnlineScore(asstUpExecution.getOnlineScore());
        asstUpExecution.setCampNumber(asstUpExecution.getCampNumber());
        asstUpExecution.setAsstNumber(asstUpExecution.getAsstNumber());
        asstUpExecution.setCampScore(asstUpExecution.getCampScore());
        asstUpExecution.setFosterNew(asstUpExecution.getFosterNew());
        asstUpExecution.setCompanyTrainNumber(asstUpExecution.getCompanyTrainNumber());
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
        Integer applicationRate = asstUpStandard.getApplicationRate();
        //统计完成度在applicationRate之上的课程数量
        Integer finish = planService.getUserPlans(profileId).stream().filter(improvementPlan -> improvementPlan.getCompleteTime() != null).map(improvementPlan -> {
            List<PracticePlan> practicePlans = planService.loadPracticePlans(improvementPlan.getId());
            Long sum = practicePlans.stream().filter(practicePlan -> (practicePlan.getType() == PracticePlan.APPLICATION) || (practicePlan.getType() == PracticePlan.APPLICATION_REVIEW)).count();
            Long count = practicePlans.stream().filter(practicePlan -> (practicePlan.getStatus() == 1) && (practicePlan.getType() == PracticePlan.APPLICATION) || (practicePlan.getType() == PracticePlan.APPLICATION_REVIEW)).count();

            if (count * 100 / sum >= applicationRate) {
                return 1;
            } else {
                return 0;
            }
        }).reduce(0, Integer::sum);

        if (asstUpStandard.getLearnedProblem() > finish) {
            return false;
        }
        return AsstHelper.checkIsReached(asstUpStandard, asstUpExecution);
    }


    /**
     * 根据昵称,学号或者riseId进行查询
     *
     * @param riseId
     * @return
     */
    private List<Integer> searchProfiles(String riseId) {
        List<Integer> profiles = accountService.loadProfilesByNickName(riseId).stream().map(Profile::getId).collect(Collectors.toList());
        if (profiles.size() == 0) {
            Profile riseProfile = accountService.getProfileByRiseId(riseId);
            if (riseProfile == null) {
                Profile memProfile = accountService.loadProfileByMemberId(riseId);
                if (memProfile != null) {
                    profiles.add(memProfile.getId());
                }
            } else {
                profiles.add(riseProfile.getId());
            }
        }
        return profiles;
    }


    private Integer unReachedCount(Integer profileId, AsstUpStandard asstUpStandard, AsstUpExecution asstUpExecution) {
        Integer unReached = 0;

        Integer applicationRate = asstUpStandard.getApplicationRate();
        //统计完成度在applicationRate之上的课程数量
        Integer finish = planService.getUserPlans(profileId).stream().filter(improvementPlan -> improvementPlan.getCompleteTime() != null).map(improvementPlan -> {
            List<PracticePlan> practicePlans = planService.loadPracticePlans(improvementPlan.getId());
            Long sum = practicePlans.stream().filter(practicePlan -> (practicePlan.getType() == PracticePlan.APPLICATION) || (practicePlan.getType() == PracticePlan.APPLICATION_REVIEW)).count();
            Long count = practicePlans.stream().filter(practicePlan -> (practicePlan.getStatus() == 1) && (practicePlan.getType() == PracticePlan.APPLICATION) || (practicePlan.getType() == PracticePlan.APPLICATION_REVIEW)).count();

            if (count * 100 / sum >= applicationRate) {
                return 1;
            } else {
                return 0;
            }
        }).reduce(0, Integer::sum);

        if (asstUpStandard.getLearnedProblem() > finish) {
            unReached++;
        }
        unReached = unReached + AsstHelper.calUnReached(asstUpStandard, asstUpExecution);

        return unReached;
    }


}
