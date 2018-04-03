package com.iquanwai.confucius.web.pc.asst.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.asst.AssistantCoachService;
import com.iquanwai.confucius.biz.domain.asst.AsstUpService;
import com.iquanwai.confucius.biz.domain.backend.BusinessSchoolService;
import com.iquanwai.confucius.biz.domain.backend.OperationManagementService;
import com.iquanwai.confucius.biz.domain.backend.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.RiseWorkInfoDto;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.TableDto;
import com.iquanwai.confucius.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.confucius.biz.po.apply.BusinessApplySubmit;
import com.iquanwai.confucius.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.apply.InterviewRecord;
import com.iquanwai.confucius.biz.po.asst.AsstUpExecution;
import com.iquanwai.confucius.biz.po.asst.AsstUpStandard;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.pc.backend.dto.BusinessApplicationDto;
import com.iquanwai.confucius.web.enums.LastVerifiedEnums;
import com.iquanwai.confucius.web.pc.asst.dto.ClassNameGroups;
import com.iquanwai.confucius.web.pc.asst.dto.Group;
import com.iquanwai.confucius.web.pc.asst.dto.InterviewDto;
import com.iquanwai.confucius.web.pc.asst.dto.UpGradeDto;
import com.iquanwai.confucius.web.pc.backend.dto.*;
import com.iquanwai.confucius.web.pc.datahelper.AsstHelper;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by justin on 17/4/27.
 */
@RestController
@RequestMapping("/pc/asst")
public class AssistantCoachController {
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AssistantCoachService assistantCoachService;
    @Autowired
    private OperationManagementService operationManagementService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private BusinessSchoolService businessSchoolService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AsstUpService asstUpService;
    @Autowired
    private PlanService planService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/application/{problemId}")
    public ResponseEntity<Map<String, Object>> getUnderCommentApplication(PCLoginUser pcLoginUser,
                                                                          @PathVariable Integer problemId) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        List<RiseWorkInfoDto> applicationSubmit = assistantCoachService.getUnderCommentApplications(problemId);

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("助教后台")
                .function("应用练习")
                .action("获取待评论的应用练习");
        operationLogService.log(operationLog);

        return WebUtils.result(applicationSubmit);
    }

    @RequestMapping(value = "/application/nickname/{problemId}/{nickName}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getUnderCommentApplicationByNickName(PCLoginUser pcLoginUser, @PathVariable Integer problemId, @PathVariable String nickName) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        List<RiseWorkInfoDto> riseWorkInfoDtos = assistantCoachService.getUnderCommentApplicationsByNickName(problemId, nickName);
        return WebUtils.result(riseWorkInfoDtos);
    }

    @RequestMapping(value = "/application/memberid/{problemId}/{memberId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getUnderCommentApplicationByMemberId(PCLoginUser pcLoginUser, @PathVariable Integer problemId, @PathVariable String memberId) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        List<RiseWorkInfoDto> riseWorkInfoDtos = assistantCoachService.getUnderCommentApplicationsByMemberId(problemId, memberId);
        return WebUtils.result(riseWorkInfoDtos);
    }

    @RequestMapping("/comment/count")
    public ResponseEntity<Map<String, Object>> getCommentCount(PCLoginUser pcLoginUser) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        Pair<Integer, Integer> counts = assistantCoachService.getCommentCount(pcLoginUser.getProfileId());
        Map<String, Integer> countMap = Maps.newHashMap();
        countMap.put("totalComment", counts.getRight());
        countMap.put("todayComment", counts.getLeft());

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("助教后台")
                .function("小课论坛")
                .action("获取待评论的小课分享");
        operationLogService.log(operationLog);

        return WebUtils.result(countMap);
    }

    @RequestMapping("/application/problem/list")
    public ResponseEntity<Map<String, Object>> loadApplicationProblems(PCLoginUser pcLoginUser) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        List<Problem> problems = problemService.loadProblems();
        List<ProblemCatalog> catalogs = problemService.loadAllCatalogs();
        Map<Integer, Integer> underCommentMap = assistantCoachService.getUnderCommentApplicationCount();
        List<ProblemCatalogDto> result = catalogs.stream().map(item -> {
            ProblemCatalogDto dto = new ProblemCatalogDto();
            List<ProblemListDto> collect = problems.stream().filter(problem -> !problem.getDel())
                    .filter(problem -> Objects.equals(problem.getCatalogId(), item.getId())).map(problem -> {
                        ProblemListDto problemList = new ProblemListDto();
                        problemList.setId(problem.getId());
                        problemList.setAbbreviation(problem.getAbbreviation());
                        problemList.setProblem(problem.getProblem());
                        problemList.setUnderCommentCount(underCommentMap.get(problem.getId()));
                        return problemList;
                    }).collect(Collectors.toList());
            dto.setProblems(collect);
            dto.setName(item.getName());
            return dto;
        }).collect(Collectors.toList());

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("助教后台")
                .function("应用练习评论")
                .action("获取问题列表");
        operationLogService.log(operationLog);
        return WebUtils.result(result);
    }

    @RequestMapping("/commented/submit")
    public ResponseEntity<Map<String, Object>> getCommentedSubmit(PCLoginUser pcLoginUser) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        List<RiseWorkInfoDto> riseWorkInfoDtos = assistantCoachService.getCommentedSubmit(pcLoginUser.getProfileId());

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("助教后台")
                .function("评论")
                .action("获取已评论文章");
        operationLogService.log(operationLog);

        return WebUtils.result(riseWorkInfoDtos);
    }

    @RequestMapping("/hot/warmup")
    public ResponseEntity<Map<String, Object>> getHotPracticeDiscuss(PCLoginUser loginUser, @ModelAttribute Page page) {
        //每页50道题目
        page.setPageSize(50);
        List<WarmupPractice> warmupPractices = operationManagementService.getLastSixtyDayActivePractice(page);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习讨论区")
                .action("加载最热的巩固练习");
        operationLogService.log(operationLog);

        RefreshListDto<WarmupPractice> refreshListDto = new RefreshListDto<>();
        refreshListDto.setEnd(page.isLastPage());
        refreshListDto.setList(warmupPractices);

        return WebUtils.result(refreshListDto);
    }

    @RequestMapping("/warmup/load/{practiceId}")
    public ResponseEntity<Map<String, Object>> getPracticeDiscuss(PCLoginUser loginUser,
                                                                  @PathVariable Integer practiceId) {
        WarmupPractice warmupPractice = operationManagementService.getWarmupPractice(practiceId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习讨论区")
                .action("加载巩固练习讨论")
                .memo(practiceId.toString());
        operationLogService.log(operationLog);

        return WebUtils.result(warmupPractice);
    }

    @RequestMapping(value = "/reply/discuss", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> replyDiscuss(PCLoginUser loginUser,
                                                            @RequestBody DiscussDto discussDto) {
        if (discussDto.getComment() == null || discussDto.getComment().length() > 1000) {
            return WebUtils.result("您提交的讨论字数过长");
        }

        operationManagementService.discuss(loginUser.getProfileId(),
                discussDto.getWarmupPracticeId(),
                discussDto.getComment(), discussDto.getRepliedId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习")
                .action("回复讨论")
                .memo(discussDto.getWarmupPracticeId().toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }
    /**
     * 获得RiseMember的班级和小组
     *
     * @param loginUser
     * @return
     */
    @RequestMapping(value = "/load/classname/group", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadClassNameAndGroup(PCLoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("助教管理")
                .action("加载班级和小组");
        operationLogService.log(operationLog);

        List<RiseClassMember> riseClassMembers = assistantCoachService.loadClassNameAndGroupId();
        ClassNameGroups classNameGroups = new ClassNameGroups();

        List<String> classNames =  riseClassMembers.stream().map(RiseClassMember::getClassName).distinct().collect(Collectors.toList());
        List<Group> groupIds = Lists.newArrayList();
        riseClassMembers.stream().forEach(riseClassMember -> {
            Group group = new Group();
            group.setClassName(riseClassMember.getClassName());
            group.setGroupId(riseClassMember.getGroupId());
            groupIds.add(group);
        });
        classNameGroups.setClassName(classNames);
        classNameGroups.setGroupIds(groupIds);

        return WebUtils.result(classNameGroups);
    }


    /**
     * 根据班级和小组搜索求点评
     * @param loginUser
     * @param problemId
     * @param className
     * @param groupId
     * @return
     */
    @RequestMapping(value = "/application/{problemId}/{className}/{groupId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadSubmitsByProblemIdClassNameGroup(PCLoginUser loginUser, @PathVariable Integer problemId, @PathVariable String className, @PathVariable String groupId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("助教管理")
                .action("根据班级和小组搜索求点评");
        operationLogService.log(operationLog);

       return WebUtils.result(assistantCoachService.getUnderCommentApplicationsByClassNameAndGroup(problemId,className,groupId));
    }

    @RequestMapping("/load/business/applications")
    public ResponseEntity<Map<String,Object>> loadBusinessApplications(PCLoginUser loginUser,@ModelAttribute Page page){
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("助教管理")
                .action("加载商学院审批");
        operationLogService.log(operationLog);
        if (page == null) {
            page = new Page();
        }
        page.setPageSize(20);

        List<BusinessSchoolApplication> applications = assistantCoachService.loadByInterviewer(loginUser.getProfileId(),page);

        TableDto<BusinessApplicationDto> result = new TableDto<>();
        result.setPage(page);
        result.setData(getBusinessApplicationDto(applications));
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/add/interview/record",method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> addInterviewRecord(PCLoginUser loginUser, @RequestBody InterviewDto interviewDto){
        logger.info(interviewDto.toString());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("助教管理")
                .action("添加面试记录");
        operationLogService.log(operationLog);
        InterviewRecord interviewRecord = new InterviewRecord();
        String[] str = {"interviewTime"};
        BeanUtils.copyProperties(interviewDto,interviewRecord,str);
        if(interviewDto.getInterviewTime()!=null) {
            interviewRecord.setInterviewTime(DateUtils.parseDateTimeToString(interviewDto.getInterviewTime()));
        }
        interviewRecord.setInterviewerId(loginUser.getProfileId());

        if(assistantCoachService.addInterviewRecord(interviewRecord)==-1){
            return WebUtils.error("添加面试记录失败");
        }
        return WebUtils.success();
    }

    /**
     * @param loginUser
     * @return
     */
    @RequestMapping("/load/up/info")
    public ResponseEntity<Map<String, Object>> loadUpGradeInfo(PCLoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("助教后台管理")
                .function("助教信息").action("加载升级信息");
        operationLogService.log(operationLog);

        AsstUpExecution asstUpExecution = asstUpService.loadUpGradeExecution(loginUser.getProfileId());
        AsstUpStandard asstUpStandard = asstUpService.loadStandard(loginUser.getProfileId());

        if(asstUpStandard==null || asstUpExecution == null){
            return WebUtils.error("没有您对应的升级信息");
        }

        UpGradeDto upGradeDto = initUpGradeDto(loginUser.getProfileId(),asstUpStandard,asstUpExecution);

        return WebUtils.result(upGradeDto);
    }


    private UpGradeDto initUpGradeDto(Integer profileId,AsstUpStandard asstUpStandard,AsstUpExecution asstUpExecution){
        UpGradeDto upGradeDto = AsstHelper.genUpGradeInfo(asstUpStandard,asstUpExecution);

        Integer applicationRate = asstUpStandard.getApplicationRate();

        Integer finish = planService.getPlans(profileId).stream().filter(improvementPlan -> improvementPlan.getCompleteTime()!=null).map(improvementPlan -> {
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
        upGradeDto.setRemainProblem(AsstHelper.getRemain(finish,total));



        return upGradeDto;

    }




    private List<BusinessApplicationDto> getBusinessApplicationDto(List<BusinessSchoolApplication> applications){
        final List<String> openidList;
        if (applications != null && applications.size() > 0) {
            //获取黑名单用户
            openidList = accountService.loadBlackListOpenIds();
        } else {
            openidList = null;
        }
        Assert.notNull(applications);
        List<BusinessApplicationDto> dtoGroup = applications.stream().map(application -> {
            Profile profile = accountService.getProfile(application.getProfileId());
            BusinessApplicationDto dto = this.initApplicationDto(application);
            List<BusinessApplyQuestion> questions = businessSchoolService.loadUserQuestions(application.getId()).stream().sorted((Comparator.comparing(BusinessApplyQuestion::getSequence))).collect(Collectors.toList());
            dto.setQuestionList(questions);
            // 查询是否会员
            RiseMember riseMember = businessSchoolService.getUserRiseMember(application.getProfileId());
            if (riseMember != null) {
                dto.setMemberTypeId(riseMember.getMemberTypeId());
                dto.setMemberType(riseMember.getName());
            }
            dto.setApplyId(application.getId());
            dto.setInterviewRecord(assistantCoachService.loadInterviewRecord(application.getId()));
            dto.setIsAsst(businessSchoolService.checkIsAsst(application.getProfileId()) ? "是" : "否");
            dto.setFinalPayStatus(businessSchoolService.queryFinalPayStatus(application.getProfileId()));
            dto.setNickname(profile.getNickname());
            dto.setOriginMemberTypeName(this.getMemberName(application.getOriginMemberType()));
            dto.setIsBlack("否");
            List<BusinessApplySubmit> businessApplySubmits = businessSchoolService.loadByApplyId(application.getId());
            dto.setIsInterviewed(assistantCoachService.loadInterviewRecord(application.getId())==null?"否":"是");
            businessApplySubmits.stream().forEach(businessApplySubmit -> {
                Integer questionId = businessApplySubmit.getQuestionId();
                if(questionId == 14){
                    dto.setInterviewTime(businessApplySubmit.getChoiceText());
                }
                if(questionId== 5 || questionId == 22){
                    dto.setWorkYear(businessApplySubmit.getChoiceText());
                }
                if(questionId == 2 || questionId == 19){
                    dto.setIndustry(businessApplySubmit.getChoiceText());
                }
                if(questionId == 6 || questionId == 23){
                    dto.setEducation(businessApplySubmit.getChoiceText());
                }
                if(questionId == 7 || questionId == 24){
                    dto.setCollege(businessApplySubmit.getUserValue());
                }
                if(questionId == 8 || questionId == 25){
                    dto.setLocation(businessApplySubmit.getUserValue());
                }
                if( questionId == 1 || questionId == 18){
                    dto.setJob(businessApplySubmit.getChoiceText());
                }
            });

            int lastVerifiedCode = application.getLastVerified();

            if (lastVerifiedCode == LastVerifiedEnums.LAST_VERIFIED_ZERO.getLastVerifiedCode()) {
                dto.setVerifiedResult(LastVerifiedEnums.LAST_VERIFIED_ZERO.getLastVerifiedMsg());
            } else if (lastVerifiedCode == LastVerifiedEnums.LAST_VERIFIED_APPROVAL.getLastVerifiedCode()) {
                dto.setVerifiedResult(LastVerifiedEnums.LAST_VERIFIED_APPROVAL.getLastVerifiedMsg());
            } else if (lastVerifiedCode == LastVerifiedEnums.LAST_VERIFIED_REJECT.getLastVerifiedCode()) {
                dto.setVerifiedResult(LastVerifiedEnums.LAST_VERIFIED_REJECT.getLastVerifiedMsg());
            } else if (lastVerifiedCode == LastVerifiedEnums.LAST_VERIFIED_IGNORE.getLastVerifiedCode()) {
                dto.setVerifiedResult(LastVerifiedEnums.LAST_VERIFIED_IGNORE.getLastVerifiedMsg());
            } else if (lastVerifiedCode == LastVerifiedEnums.LAST_VERIFIED_EXPIRED.getLastVerifiedCode()) {
                dto.setVerifiedResult(LastVerifiedEnums.LAST_VERIFIED_EXPIRED.getLastVerifiedMsg());
            } else {
                dto.setVerifiedResult("未知");
            }

            if (openidList!=null && CollectionUtils.isNotEmpty(openidList)) {
                if (openidList.contains(profile.getOpenid())) {
                    dto.setIsBlack("是");
                }
            }
            dto.setReward(businessSchoolService.loadUserAuditionReward(application.getProfileId()));
            dto.setSubmitTime(DateUtils.parseDateTimeToString(application.getAddTime()));
            dto.setOrderId(application.getOrderId());
            if (application.getInterviewer() != null) {
                Profile interviewer = accountService.getProfile(application.getInterviewer());
                dto.setInterviewer(application.getInterviewer());
                dto.setInterviewerName(interviewer.getNickname());
            }

            return dto;
        }).collect(Collectors.toList());

        return dtoGroup;
    }

    private String getMemberName(Integer type) {
        if (type == null) {
            return "非会员";
        }
        switch (type) {
            case RiseMember.ELITE:
                return "商学院";
            case RiseMember.HALF_ELITE:
                return "精英半年";
            case RiseMember.HALF:
                return "专业半年";
            case RiseMember.ANNUAL:
                return "专业一年";
            case RiseMember.CAMP:
                return "专项课";
            default:
                return "异常数据";
        }
    }

    private BusinessApplicationDto initApplicationDto(BusinessSchoolApplication application) {
        BusinessApplicationDto dto = new BusinessApplicationDto();
        dto.setSubmitId(application.getSubmitId());
        dto.setIsDuplicate(application.getIsDuplicate() ? "是" : "否");
        if (application.getCoupon() != null) {
            dto.setCoupon(new BigDecimal(application.getCoupon()).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        } else {
            dto.setCoupon("无");
        }
        dto.setOriginMemberType(application.getOriginMemberType());
        dto.setDeal(application.getDeal());
        dto.setComment(application.getComment());
        dto.setId(application.getId());
        dto.setProfileId(application.getProfileId());
        dto.setStatus(application.getStatus());
        dto.setCheckTime(application.getCheckTime() == null ? "未审核" : DateUtils.parseDateToString(application.getCheckTime()));
        dto.setDel(application.getDel());
        return dto;
    }



}
