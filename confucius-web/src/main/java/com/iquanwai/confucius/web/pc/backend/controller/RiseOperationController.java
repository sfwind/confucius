package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.backend.BusinessSchoolService;
import com.iquanwai.confucius.biz.domain.backend.OperationManagementService;
import com.iquanwai.confucius.biz.domain.backend.ProblemService;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.survey.SurveyService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.TableDto;
import com.iquanwai.confucius.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.confucius.biz.po.common.customer.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.po.common.survey.SurveyHref;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQPublisher;
import com.iquanwai.confucius.web.course.dto.backend.ApplicationDto;
import com.iquanwai.confucius.web.enums.AssistCatalogEnums;
import com.iquanwai.confucius.web.enums.LastVerifiedEnums;
import com.iquanwai.confucius.web.pc.backend.dto.ApproveDto;
import com.iquanwai.confucius.web.pc.backend.dto.AssignDto;
import com.iquanwai.confucius.web.pc.backend.dto.ProblemCatalogDto;
import com.iquanwai.confucius.web.pc.backend.dto.ProblemListDto;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by justin on 17/3/16.
 * RISE运营相关的接口
 */
@RestController
@RequestMapping("/pc/operation")
public class RiseOperationController {
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private OperationManagementService operationManagementService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private BusinessSchoolService businessSchoolService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private SignupService signupService;
    @Autowired
    private PayService payService;


    private static final String SEARCH_TOPIC = "business_school_application_search";
    private static final String NOTICE_TOPIC = "business_school_application_notice";

    private RabbitMQPublisher searchPublisher;
    private RabbitMQPublisher noticePublisher;

    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final int APPLICATION_SUBMIT_SIZE = 20;

    @PostConstruct
    public void init() {
        searchPublisher = rabbitMQFactory.initFanoutPublisher(SEARCH_TOPIC);
        noticePublisher = rabbitMQFactory.initFanoutPublisher(NOTICE_TOPIC);
    }

    @RequestMapping(value = "/search/bs/application/{date}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> searchApplication(PCLoginUser loginUser, @PathVariable String date) {
        LOGGER.info("搜索{} 申请", date);
        try {
            searchPublisher.publish(date);
        } catch (ConnectException e) {
            LOGGER.error("发送申请搜索mq失败:{}", date);
            return WebUtils.error("发送申请搜索mq失败:" + date);
        }
        return WebUtils.success();
    }

    @RequestMapping(value = "/notice/bs/application/{date}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> noticeApplication(PCLoginUser loginUser, @PathVariable String date) {
        LOGGER.info("发送{} 提醒", date);
        try {
            noticePublisher.publish(date);
        } catch (ConnectException e) {
            LOGGER.error("发送提醒mq失败：{}", date);
            return WebUtils.error("发送提醒mq失败:" + date);
        }
        return WebUtils.success();
    }


    @RequestMapping("/application/submit/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadApplicationSubmit(PCLoginUser loginUser,
                                                                     @PathVariable Integer applicationId,
                                                                     @ModelAttribute Page page) {
        page.setPageSize(APPLICATION_SUBMIT_SIZE);
        List<ApplicationSubmit> applicationSubmitList = operationManagementService.loadApplicationSubmit(applicationId, page);

        applicationSubmitList.stream().forEach(applicationSubmit -> {
            Boolean isComment = operationManagementService.isComment(applicationSubmit.getId(), loginUser.getProfileId());
            applicationSubmit.setComment(isComment ? 1 : 0);
        });

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("应用练习提交")
                .action("加载应用练习提交")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);

        return WebUtils.result(applicationSubmitList);
    }

    @RequestMapping(value = "/highlight/discuss/{discussId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> highlightDiscuss(PCLoginUser loginUser,
                                                                @PathVariable Integer discussId) {

        operationManagementService.highlightDiscuss(discussId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习")
                .action("加精讨论")
                .memo(discussId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/highlight/applicationSubmit/{practiceId}/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> highlightApplicationSubmit(PCLoginUser loginUser,
                                                                          @PathVariable Integer practiceId,
                                                                          @PathVariable Integer submitId) {

        operationManagementService.highlightApplicationSubmit(practiceId, submitId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("应用练习")
                .action("加精优秀的作业")
                .memo(submitId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping("/problem/list")
    public ResponseEntity<Map<String, Object>> loadProblems(PCLoginUser pcLoginUser) {

        List<Problem> problems = problemService.loadProblems();
        List<ProblemCatalog> catalogs = problemService.loadAllCatalogs();
        List<ProblemCatalogDto> result = catalogs.stream().map(item -> {
            ProblemCatalogDto dto = new ProblemCatalogDto();
            List<ProblemListDto> collect = problems.stream().filter(problem -> !problem.getDel())
                    .filter(problem -> Objects.equals(problem.getCatalogId(), item.getId())).map(problem -> {
                        ProblemListDto problemList = new ProblemListDto();
                        problemList.setId(problem.getId());
                        problemList.setProblem(problem.getProblem());
                        return problemList;
                    }).collect(Collectors.toList());
            dto.setProblems(collect);
            dto.setName(item.getName());
            return dto;
        }).collect(Collectors.toList());


        OperationLog operationLog = OperationLog.create().openid(pcLoginUser == null ? null : pcLoginUser.getOpenId())
                .module("内容运营")
                .function("应用练习")
                .action("获取问题列表");
        operationLogService.log(operationLog);

        return WebUtils.result(result);
    }

    /**
     * 删除助教的巩固练习评论
     */
    @RequestMapping("/warmup/discuss/del/{discussId}")
    public ResponseEntity<Map<String, Object>> deleteWarmupDiscuss(PCLoginUser loginUser, @PathVariable Integer discussId) {
        Integer result = operationManagementService.deleteAsstWarmupDiscuss(discussId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习练习区")
                .action("删除巩固练习评论")
                .memo(discussId.toString());
        operationLogService.log(operationLog);
        if (result == 1) {
            return WebUtils.success();
        } else if (result == 0) {
            return WebUtils.error(201, "抱歉，暂时不能删除非助教评论");
        } else {
            return WebUtils.error("系统异常");
        }
    }

    /**
     * 碎片化总任务列表加载
     *
     * @param problemId   问题id
     * @param pcLoginUser 登陆人
     */
    @RequestMapping("/homework/{problemId}")
    public ResponseEntity<Map<String, Object>> getProblemHomeworkList(@PathVariable Integer problemId, PCLoginUser pcLoginUser) {
        Assert.notNull(pcLoginUser, "用户信息能不能为空");
        List<ApplicationPractice> applicationPractices = practiceService.loadApplicationByProblemId(problemId);
        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("总任务列表加载")
                .memo(problemId + "");
        operationLogService.log(operationLog);

        return WebUtils.result(applicationPractices);
    }

    @RequestMapping(value = "/bs/application/list", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadApplicationList(@ModelAttribute Page page, LoginUser loginUser) {
        OperationLog operationLog = OperationLog.create()
                .module("后台功能")
                .function("商学院申请")
                .action("加载申请列表");
        operationLogService.log(operationLog);
        if (page == null) {
            page = new Page();
        }
        page.setPageSize(20);

        List<BusinessSchoolApplication> applications = businessSchoolService.loadBusinessSchoolList(page);
        final List<String> openidList;
        if (applications != null && applications.size() > 0) {
            //获取黑名单用户
            openidList = accountService.loadBlackListOpenIds();
        } else {
            openidList = null;
        }

        Assert.notNull(applications);
        List<ApplicationDto> dtoGroup = applications.stream().map(application -> {
            Profile profile = accountService.getProfile(application.getProfileId());
            ApplicationDto dto = this.initApplicationDto(application);
            List<BusinessApplyQuestion> questions = businessSchoolService.loadUserQuestions(application.getId()).stream().sorted((Comparator.comparing(BusinessApplyQuestion::getSequence))).collect(Collectors.toList());
            dto.setQuestionList(questions);
            // 查询是否会员
            RiseMember riseMember = businessSchoolService.getUserRiseMember(application.getProfileId());
            if (riseMember != null) {
                dto.setMemberTypeId(riseMember.getMemberTypeId());
                dto.setMemberType(riseMember.getName());
            }
            dto.setIsAsst(businessSchoolService.checkIsAsst(application.getProfileId()) ? "是" : "否");
            dto.setFinalPayStatus(businessSchoolService.queryFinalPayStatus(application.getProfileId()));
            dto.setNickname(profile.getNickname());
            dto.setOriginMemberTypeName(this.getMemberName(application.getOriginMemberType()));
            dto.setIsBlack("否");

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

            if (openidList != null && (openidList.size() > 0)) {
                if (openidList.stream().filter(openid -> openid.contains(application.getOpenid())).count() > 0) {
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
        TableDto<ApplicationDto> result = new TableDto<>();
        result.setPage(page);
        result.setData(dtoGroup);
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/bs/application/reject", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> rejectApplication(LoginUser loginUser, @RequestBody ApproveDto approveDto) {
        OperationLog operationLog = OperationLog.create()
                .module("后台功能")
                .function("商学院申请")
                .action("拒绝")
                .memo(approveDto.getId() + "");
        operationLogService.log(operationLog);
        BusinessSchoolApplication application = businessSchoolService.loadBusinessSchoolApplication(approveDto.getId());
        if (application == null) {
            return WebUtils.error("该申请不存在");
        } else {
            boolean reject = businessSchoolService.rejectApplication(application.getId(), approveDto.getComment());
            if (reject) {
                String orderId = application.getOrderId();
                if (orderId != null) {
                    QuanwaiOrder quanwaiOrder = signupService.getQuanwaiOrder(orderId);
                    if (quanwaiOrder != null) {
                        if (quanwaiOrder.getStatus().equals(QuanwaiOrder.PAID) || quanwaiOrder.getStatus().equals(QuanwaiOrder.REFUND_FAILED)) {
                            // 开始退款
                            payService.refund(orderId, quanwaiOrder.getPrice());
                        }
                    }
                }
                return WebUtils.success();
            } else {
                return WebUtils.error("更新失败");
            }
        }
    }

    @RequestMapping(value = "/bs/application/approve", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> approveApplication(LoginUser loginUser, @RequestBody ApproveDto approveDto) {
        OperationLog operationLog = OperationLog.create()
                .module("后台功能")
                .function("商学院申请")
                .action("通过")
                .memo(approveDto.getId() + "");
        operationLogService.log(operationLog);
        BusinessSchoolApplication application = businessSchoolService.loadBusinessSchoolApplication(approveDto.getId());
        if (application == null) {
            return WebUtils.error("该申请不存在");
        } else {
            if (approveDto.getCoupon() == null) {
                approveDto.setCoupon(0d);
            }
            boolean approve = businessSchoolService.approveApplication(approveDto.getId(), approveDto.getCoupon(), approveDto.getComment());
            if (approve) {
                return WebUtils.success();
            } else {
                return WebUtils.error("更新失败");
            }
        }
    }

    @RequestMapping(value = "/bs/application/ignore", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> ignoreApplication(LoginUser loginUser, @RequestBody ApproveDto approveDto) {
        OperationLog operationLog = OperationLog.create()
                .module("后台功能")
                .function("商学院申请")
                .action("私信")
                .memo(approveDto.getId() + "");
        operationLogService.log(operationLog);
        BusinessSchoolApplication application = businessSchoolService.loadBusinessSchoolApplication(approveDto.getId());
        if (application == null) {
            return WebUtils.error("该申请不存在");
        } else {
            boolean approve = businessSchoolService.ignoreApplication(approveDto.getId(), approveDto.getComment());
            if (approve) {
                return WebUtils.success();
            } else {
                return WebUtils.error("更新失败");
            }
        }
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
                return "训练营";
            default:
                return "异常数据";
        }
    }

    private ApplicationDto initApplicationDto(BusinessSchoolApplication application) {
        ApplicationDto dto = new ApplicationDto();
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
        dto.setOpenid(application.getOpenid());
        dto.setStatus(application.getStatus());
        dto.setCheckTime(application.getCheckTime() == null ? "未审核" : DateUtils.parseDateToString(application.getCheckTime()));
        dto.setDel(application.getDel());
        return dto;
    }

    @RequestMapping(value = "/survey/config/list", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadConfigList(PCLoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理")
                .function("问卷星")
                .action("查看问卷配置");
        operationLogService.log(operationLog);
        List<SurveyHref> surveyHrefs = surveyService.loadAllSurveyHref();
        return WebUtils.result(surveyHrefs);
    }

    @RequestMapping(value = "/survey/config", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateSurveyConfig(PCLoginUser loginUser,
                                                                  @RequestBody SurveyHref surveyHref) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(surveyHref, "问卷不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理")
                .function("问卷星")
                .action("更新问卷配置");
        operationLogService.log(operationLog);
        Boolean updateStatus = surveyService.updateSurveyHref(surveyHref);
        return WebUtils.result(updateStatus);
    }

    @RequestMapping(value = "/delete/survey/config/{id}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> deleteSurveyConfig(PCLoginUser loginUser, @PathVariable Integer id) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(id, "问卷id不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理")
                .function("问卷星")
                .action("删除问卷配置");
        operationLogService.log(operationLog);
        Boolean deleteStatus = surveyService.deleteSurveyHref(id);
        return WebUtils.result(deleteStatus);
    }

    @RequestMapping(value = "/interviewer/list", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadInterviewerList(PCLoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理")
                .function("审批")
                .action("获取审批者列表");
        operationLogService.log(operationLog);
        List<UserRole> userRoles = businessSchoolService.loadAssts();
        userRoles.forEach(item -> {
            Profile profile = accountService.getProfile(item.getProfileId());
            item.setAsstName(profile.getNickname());
            AssistCatalogEnums role = AssistCatalogEnums.getById(item.getRoleId());
            item.setAsstType(role == null ? "" : role.getRoleName());
            item.setLevel(role == null ? -10 : role.getLevel());
        });
        userRoles.sort(((o1, o2) -> o2.getLevel() - o1.getLevel()));
        return WebUtils.result(userRoles);
    }

    @RequestMapping(value = "/assign/interviewer", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> assignInterviewer(PCLoginUser loginUser, @RequestBody AssignDto assignDto) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理")
                .function("审批")
                .action("分配审批者");
        operationLogService.log(operationLog);
        Integer result = businessSchoolService.assignInterviewer(assignDto.getApplyId(), assignDto.getProfileId());
        if (result != null && result > 0) {
            return WebUtils.success();
        } else {
            return WebUtils.error("分配失败");
        }
    }
}
