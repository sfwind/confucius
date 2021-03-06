package com.iquanwai.confucius.web.pc.backend.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.asst.AssistantCoachService;
import com.iquanwai.confucius.biz.domain.backend.BusinessSchoolService;
import com.iquanwai.confucius.biz.domain.backend.OperationManagementService;
import com.iquanwai.confucius.biz.domain.backend.ProblemService;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.survey.SurveyService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.template.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.template.TemplateMessageService;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import com.iquanwai.confucius.biz.exception.RefundException;
import com.iquanwai.confucius.biz.po.ActionLog;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.QuanwaiOrder;
import com.iquanwai.confucius.biz.po.TableDto;
import com.iquanwai.confucius.biz.po.TemplateMsg;
import com.iquanwai.confucius.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.confucius.biz.po.apply.BusinessApplySubmit;
import com.iquanwai.confucius.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.confucius.biz.po.apply.InterviewRecord;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.po.common.survey.SurveyHref;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationPractice;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemCatalog;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.ThreadPool;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQPublisher;
import com.iquanwai.confucius.web.enums.AssistCatalogEnums;
import com.iquanwai.confucius.web.enums.LastVerifiedEnums;
import com.iquanwai.confucius.web.enums.MemberTypeEnums;
import com.iquanwai.confucius.web.enums.ProjectEnums;
import com.iquanwai.confucius.web.pc.asst.dto.InterviewDto;
import com.iquanwai.confucius.web.pc.backend.dto.ApproveDto;
import com.iquanwai.confucius.web.pc.backend.dto.AssignDto;
import com.iquanwai.confucius.web.pc.backend.dto.BusinessApplicationDto;
import com.iquanwai.confucius.web.pc.backend.dto.ProblemCatalogDto;
import com.iquanwai.confucius.web.pc.backend.dto.ProblemListDto;
import com.iquanwai.confucius.web.pc.backend.dto.TemplateDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.resolver.UnionUser;
import com.iquanwai.confucius.web.util.HandleStringUtils;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
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
    @Autowired
    private AssistantCoachService assistantCoachService;
    @Autowired
    private TemplateMessageService templateMessageService;

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
    public ResponseEntity<Map<String, Object>> searchApplication(@PathVariable String date) {
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
    public ResponseEntity<Map<String, Object>> noticeApplication(@PathVariable String date) {
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
    public ResponseEntity<Map<String, Object>> loadApplicationSubmit(PCLoginUser unionUser,
                                                                     @PathVariable Integer applicationId,
                                                                     @ModelAttribute Page page,@RequestParam("show") String show) {
        page.setPageSize(APPLICATION_SUBMIT_SIZE);
        List<ApplicationSubmit> applicationSubmitList = operationManagementService.loadApplicationSubmit(applicationId, page,show);

        applicationSubmitList.stream().forEach(applicationSubmit -> {
            Boolean isComment = operationManagementService.isComment(applicationSubmit.getId(), unionUser.getProfileId());
            applicationSubmit.setComment(isComment ? 1 : 0);
        });

        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("内容运营")
                .function("应用练习提交")
                .action("加载应用练习提交")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);

        return WebUtils.result(applicationSubmitList);
    }

    @RequestMapping(value = "/highlight/discuss/{discussId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> highlightDiscuss(UnionUser unionUser,
                                                                @PathVariable Integer discussId) {

        operationManagementService.highlightDiscuss(discussId);
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("内容运营")
                .function("巩固练习")
                .action("加精讨论")
                .memo(discussId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/highlight/cancel/discuss/{discussId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> unhighlightDiscuss(PCLoginUser unionUser,
                                                                  @PathVariable Integer discussId) {

        operationManagementService.unhighlightDiscuss(discussId);
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("内容运营")
                .function("巩固练习")
                .action("取消加精")
                .memo(discussId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/highlight/applicationSubmit/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> highlightApplicationSubmit(PCLoginUser unionUser,
                                                                          @PathVariable Integer submitId) {

        operationManagementService.highlightApplicationSubmit(submitId);
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("内容运营")
                .function("应用练习")
                .action("加精优秀的作业")
                .memo(submitId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/highlight/cancel/applicationSubmit/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> unhighlightApplicationSubmit(PCLoginUser unionUser,
                                                                            @PathVariable Integer submitId) {

        operationManagementService.unhighlightApplicationSubmit(submitId);
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("内容运营")
                .function("应用练习")
                .action("加精优秀的作业")
                .memo(submitId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping("/problem/list")
    public ResponseEntity<Map<String, Object>> loadProblems(UnionUser pcLoginUser) {

        List<Problem> problems = problemService.loadProblems();
        List<Integer> yesterdayProblems = practiceService.loadProblemsByYesterdayComments();
        List<ProblemCatalog> catalogs = problemService.loadAllCatalogs();
        List<ProblemCatalogDto> result = catalogs.stream().map(item -> {
            ProblemCatalogDto dto = new ProblemCatalogDto();
            List<ProblemListDto> collect = problems.stream().filter(problem -> !problem.getDel())
                    .filter(problem -> Objects.equals(problem.getCatalogId(), item.getId())).map(problem -> {
                        ProblemListDto problemList = new ProblemListDto();
                        problemList.setId(problem.getId());
                        problemList.setProblem(problem.getProblem());
                        problemList.setAbbreviation(problem.getAbbreviation());
                        problemList.setHasNewComments(yesterdayProblems.stream().filter(problemId -> problemId.equals(problem.getId())).count() > 0);

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
    public ResponseEntity<Map<String, Object>> deleteWarmupDiscuss(UnionUser unionUser, @PathVariable Integer discussId) {
        Integer result = operationManagementService.deleteAsstWarmupDiscuss(discussId);
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
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
    public ResponseEntity<Map<String, Object>> getProblemHomeworkList(@PathVariable Integer problemId, UnionUser pcLoginUser) {
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
    public ResponseEntity<Map<String, Object>> loadApplicationList(@ModelAttribute Page page) {
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
        Assert.notNull(applications);
        TableDto<BusinessApplicationDto> result = new TableDto<>();
        result.setPage(page);
        result.setData(getApplicationDto(applications));
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/bs/application/reject", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> rejectApplication(PCLoginUser unionUser, @RequestBody ApproveDto approveDto) {
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
            InterviewRecord interviewRecord = convertInterview(approveDto.getInterviewDto());
            if (interviewRecord == null) {
                return WebUtils.error("更新失败");
            }
            interviewRecord.setApprovalId(unionUser.getProfileId());
            interviewRecord.setAdmit(1);
            if (assistantCoachService.addInterviewRecord(interviewRecord) == -1) {
                return WebUtils.error("更新失败");
            }
            boolean reject = businessSchoolService.rejectApplication(application.getId(), "");
            if (reject) {
                String orderId = application.getOrderId();
                if (orderId != null) {
                    QuanwaiOrder quanwaiOrder = signupService.getQuanwaiOrder(orderId);
                    if (quanwaiOrder != null) {
                        if (quanwaiOrder.getStatus().equals(QuanwaiOrder.PAID) || quanwaiOrder.getStatus().equals(QuanwaiOrder.REFUND_FAILED)) {
                            // 开始退款
                            try {
                                payService.refund(orderId, quanwaiOrder.getPrice());
                            } catch (RefundException e) {
                                LOGGER.error("退款失败:{}", orderId);
                            }
                        }
                    }
                }
                operationLogService.trace(application.getProfileId(), "phoneCheck",
                        () -> {
                            OperationLogService.Prop prop = OperationLogService.props().add("checkStatus", BusinessSchoolApplication.REJECT);
                            if (application.getInterviewer() != null) {
                                prop.add("auditor", application.getInterviewer());
                            }
                            return prop;
                        });

                return WebUtils.success();
            } else {
                return WebUtils.error("更新失败");
            }
        }
    }

    @RequestMapping(value = "/bs/application/approve", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> approveApplication(PCLoginUser unionUser, @RequestBody ApproveDto approveDto) {
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("后台功能").function("商学院申请").action("通过").memo(approveDto.getId() + "");
        operationLogService.log(operationLog);

        BusinessSchoolApplication application = businessSchoolService.loadBusinessSchoolApplication(approveDto.getId());
        if (application == null) {
            return WebUtils.error("该申请不存在");
        } else {
            if (approveDto.getCoupon() == null) {
                approveDto.setCoupon(0d);
            }
            InterviewRecord interviewRecord = convertInterview(approveDto.getInterviewDto());

            if (interviewRecord == null) {
                return WebUtils.error("更新失败");
            }
            interviewRecord.setApprovalId(unionUser.getProfileId());
            if (assistantCoachService.addInterviewRecord(interviewRecord) == -1) {
                return WebUtils.error("更新失败");
            }
            boolean approve = businessSchoolService.approveApplication(approveDto.getId(), approveDto.getCoupon(), "");
            if (approve) {
                String orderId = application.getOrderId();
                if (orderId != null) {
                    QuanwaiOrder quanwaiOrder = signupService.getQuanwaiOrder(orderId);
                    if (quanwaiOrder != null) {
                        if (quanwaiOrder.getStatus().equals(QuanwaiOrder.PAID) || quanwaiOrder.getStatus().equals(QuanwaiOrder.REFUND_FAILED)) {
                            // 开始退款
                            try {
                                payService.refund(orderId, quanwaiOrder.getPrice());
                            } catch (RefundException e) {
                                LOGGER.error("退款失败:{}", orderId);
                            }
                        }
                    }
                }
                operationLogService.trace(application.getProfileId(), "phoneCheck",
                        () -> {
                            OperationLogService.Prop prop = OperationLogService.props()
                                    .add("checkStatus", BusinessSchoolApplication.APPROVE);
                            if (application.getInterviewer() != null) {
                                prop.add("auditor", application.getInterviewer());
                            }
                            prop.add("coupon", application.getCoupon() != null ? application.getCoupon() : 0);
                            return prop;
                        });

                return WebUtils.success();
            } else {
                return WebUtils.error("更新失败");
            }
        }
    }

    @RequestMapping(value = "/bs/application/ignore", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> ignoreApplication(PCLoginUser unionUser, @RequestBody ApproveDto approveDto) {
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
            InterviewRecord interviewRecord = convertInterview(approveDto.getInterviewDto());
            if (interviewRecord == null) {
                return WebUtils.error("更新失败");
            }
            interviewRecord.setApprovalId(unionUser.getProfileId());
            if (assistantCoachService.addInterviewRecord(interviewRecord) == -1) {
                return WebUtils.error("更新失败");
            }
            boolean approve = businessSchoolService.ignoreApplication(approveDto.getId(), "");
            if (approve) {
                operationLogService.trace(application.getProfileId(), "phoneCheck",
                        () -> {
                            OperationLogService.Prop prop = OperationLogService.props().add("checkStatus", BusinessSchoolApplication.IGNORE);
                            if (application.getInterviewer() != null) {
                                prop.add("auditor", application.getInterviewer());
                            }
                            return prop;
                        });
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

    @RequestMapping(value = "/survey/config/list", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadConfigList(PCLoginUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("后台管理")
                .function("问卷星")
                .action("查看问卷配置");
        operationLogService.log(operationLog);
        List<SurveyHref> surveyHrefs = surveyService.loadAllSurveyHref();
        return WebUtils.result(surveyHrefs);
    }

    @RequestMapping(value = "/survey/config", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateSurveyConfig(PCLoginUser unionUser,
                                                                  @RequestBody SurveyHref surveyHref) {
        Assert.notNull(unionUser, "用户不能为空");
        Assert.notNull(surveyHref, "问卷不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("后台管理")
                .function("问卷星")
                .action("更新问卷配置");
        operationLogService.log(operationLog);
        Boolean updateStatus = surveyService.updateSurveyHref(surveyHref);
        return WebUtils.result(updateStatus);
    }

    @RequestMapping(value = "/delete/survey/config/{id}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> deleteSurveyConfig(PCLoginUser unionUser, @PathVariable Integer id) {
        Assert.notNull(unionUser, "用户不能为空");
        Assert.notNull(id, "问卷id不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("后台管理")
                .function("问卷星")
                .action("删除问卷配置");
        operationLogService.log(operationLog);
        Boolean deleteStatus = surveyService.deleteSurveyHref(id);
        return WebUtils.result(deleteStatus);
    }

    @RequestMapping(value = "/interviewer/list", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadInterviewerList(PCLoginUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("后台管理")
                .function("审批")
                .action("获取审批者列表");
        operationLogService.log(operationLog);
        List<UserRole> userRoles = businessSchoolService.loadInterviewer();
        userRoles.forEach(item -> {
            Profile profile = accountService.getProfile(item.getProfileId());
            item.setAsstName(profile.getNickname());
            AssistCatalogEnums role = AssistCatalogEnums.getById(item.getRoleId());
            item.setAsstType(role == null ? "" : role.getRoleName());
            item.setLevel(role == null ? -10 : role.getLevel());
            item.setAssignCount(assistantCoachService.loadAssignedCount(item.getProfileId()));
        });
        userRoles.sort(((o1, o2) -> o2.getLevel() - o1.getLevel()));
        return WebUtils.result(userRoles);
    }

    @RequestMapping(value = "/assign/interviewer", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> assignInterviewer(UnionUser unionUser, @RequestBody AssignDto assignDto) {
        Assert.notNull(unionUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
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


    @RequestMapping(value = "/load/templates", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadTemplates() {
        List<TemplateMsg> templateMsgList = templateMessageService.loadTemplateMsgs();

        return WebUtils.result(templateMsgList);
    }

    /**
     * 运营后台发送模板消息接口
     */
    @RequestMapping(value = "/send/template/msg", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> sendTemplateMsg(UnionUser unionUser, @RequestBody TemplateDto templateDto) {
        String comment = templateDto.getComment();
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("运营功能").function("发送模板消息").action(comment);

        operationLogService.log(operationLog);
        String source = templateDto.getSource();
        if (source == null || HandleStringUtils.hasChinese(source)) {
            return WebUtils.error("英文消息用途是必填字段并且值不能含中文!");
        }
        List<String> openIds = Lists.newArrayList();
        if (templateDto.getIsMime()) {
            templateDto.setForcePush(true);
            openIds.add(unionUser.getOpenId());
        } else {
            List<String> tempList = Arrays.asList(templateDto.getOpenIds().split("\n"));
            openIds = new ArrayList<>(tempList);
            List<String> excludeList = Arrays.asList(templateDto.getExcludeOpenIds().split("\n"));
            //排除人数
            openIds = openIds.stream().filter(openId -> !excludeList.contains(openId)).collect(Collectors.toList());
        }
        Integer templateId = templateDto.getTemplateId();
        String templateMsgId = templateMessageService.getTemplateIdByDB(templateId);

        List<String> blackLists = accountService.loadBlackListOpenIds();
        Boolean forcePush = templateDto.getForcePush();
        //过滤黑名单用户
        List<String> sendLists = openIds.stream().distinct().filter(openId -> !blackLists.contains(openId)).collect(Collectors.toList());
        ThreadPool.execute(() -> {
            try {
                sendLists.forEach(openid -> {
                    TemplateMessage templateMessage = new TemplateMessage();
                    templateMessage.setTouser(openid);

                    templateMessage.setTemplate_id(templateMsgId);
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setData(data);
                    if (templateDto.getFirst() != null) {
                        String first = templateDto.getFirst();
                        if (first.contains("{username}")) {
                            first = replaceNickname(openid, first);
                        }
                        data.put("first", new TemplateMessage.Keyword(first));
                    }
                    if (templateDto.getKeyword1() != null) {
                        String keyword1 = templateDto.getKeyword1();
                        if (keyword1.contains("{username}")) {
                            keyword1 = replaceNickname(openid, keyword1);
                        }
                        data.put("keyword1", new TemplateMessage.Keyword(keyword1));
                    }
                    if (templateDto.getKeyword2() != null) {
                        String keyword2 = templateDto.getKeyword2();
                        if (keyword2.contains("{username}")) {
                            keyword2 = replaceNickname(openid, keyword2);
                        }
                        data.put("keyword2", new TemplateMessage.Keyword(keyword2));
                    }
                    if (templateDto.getKeyword3() != null) {
                        String keyword3 = templateDto.getKeyword3();
                        if (keyword3.contains("{username}")) {
                            keyword3 = replaceNickname(openid, keyword3);
                        }
                        data.put("keyword3", new TemplateMessage.Keyword(keyword3));
                    }
                    if (templateDto.getRemark() != null) {
                        String remark = templateDto.getRemark();
                        if (remark.contains("{username}")) {
                            remark = replaceNickname(openid, remark);
                        }
                        data.put("remark", new TemplateMessage.Keyword(remark, "#FFA500"));
                    }
                    String url = templateDto.getUrl();
                    if (url != null && url.length() > 0) {
                        String redirectUrl = URLEncoder.encode(url);
                        url = ConfigUtils.domainName() + "/redirect/template/message?key=" + source + "&url=" + redirectUrl;
                        templateMessage.setUrl(url);
                    }
                    templateMessage.setComment(templateDto.getComment());
                    if (openid.equals(unionUser.getOpenId())) {
                        templateMessageService.sendMessage(templateMessage, false, source);
                    } else {
                        templateMessageService.sendMessage(templateMessage, forcePush == null || !forcePush, source);
                    }
                });
                if (!templateDto.getIsMime()) {
                    templateMessageService.sendSelfCompleteMessage(templateDto.getKeyword1(), unionUser.getOpenId());
                }
            } catch (Exception e) {
                LOGGER.error("发送通知失败", e);
            }
        });
        return WebUtils.result("正在发送中，如果你收到模板消息，则已经全部发送结束");
    }

    /**
     * 给用户开通 vip 级别的会员身份
     */
    @RequestMapping(value = "/add/member/vip", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addVipRiseMember(UnionUser unionUser,
                                                                @RequestParam("riseId") String riseId,
                                                                @RequestParam("memo") String memo,
                                                                @RequestParam("month") Integer month) {
        Pair<Integer, String> pair = accountService.addVipRiseMember(riseId, memo, month);
        if (pair.getLeft() > 0) {
            ActionLog actionLog = ActionLog.create()
                    .uid(unionUser.getId()).module("打点")
                    .action("后台操作").function("添加 vip 会员")
                    .memo("riseid：" + riseId + "，month：" + month);
            operationLogService.log(actionLog);
            return WebUtils.success();
        } else {
            return WebUtils.error(pair.getRight());
        }
    }

    private List<BusinessApplicationDto> getApplicationDto(List<BusinessSchoolApplication> applications) {
        final List<String> openidList;
        if (applications != null && applications.size() > 0) {
            //获取黑名单用户
            openidList = accountService.loadBlackListOpenIds();
        } else {
            openidList = null;
        }
        List<BusinessApplicationDto> dtoGroup = applications.stream().map(application -> {
            Profile profile = accountService.getProfile(application.getProfileId());
            BusinessApplicationDto dto = this.initApplicationDto(application);
            List<BusinessApplyQuestion> questions = businessSchoolService.loadUserQuestions(application.getId()).stream().sorted((Comparator.comparing(BusinessApplyQuestion::getSequence))).collect(Collectors.toList());
            BusinessApplyQuestion levelQuestion = questions.get(3);
            if (levelQuestion != null) {
                dto.setLevel(levelQuestion.getAnswer());
            }
            dto.setQuestionList(questions);
            // 查询是否会员
            String riseMemberNames = businessSchoolService.getUserRiseMemberNames(application.getProfileId());
            dto.setMemberType(riseMemberNames);
            dto.setApplyId(application.getId());
            dto.setInterviewRecord(assistantCoachService.loadInterviewRecord(application.getId()));
            dto.setIsAsst(businessSchoolService.checkIsAsst(application.getProfileId()) ? "是" : "否");
            dto.setFinalPayStatus(businessSchoolService.queryFinalPayStatus(application.getProfileId()));
            dto.setNickname(profile.getNickname());
            dto.setOriginMemberTypeName(this.getMemberName(application.getOriginMemberType()));
            dto.setIsBlack("否");
            MemberTypeEnums memberTypeEnums = MemberTypeEnums.getById(application.getMemberTypeId());
            if (memberTypeEnums != null) {
                dto.setProject(memberTypeEnums.getMemberTypeName());
            }
            dto.setIsInterviewed(assistantCoachService.loadInterviewRecord(application.getId()) == null ? "否" : "是");
            List<BusinessApplySubmit> businessApplySubmits = businessSchoolService.loadByApplyId(application.getId());
            businessApplySubmits.stream().forEach(businessApplySubmit -> {
                Integer questionId = businessApplySubmit.getQuestionId();
                if (questionId == 14) {
                    dto.setInterviewTime(businessApplySubmit.getChoiceText());
                }
                if (questionId == 5 || questionId == 22) {
                    dto.setWorkYear(businessApplySubmit.getChoiceText());
                }
                if (questionId == 2 || questionId == 19) {
                    dto.setIndustry(businessApplySubmit.getChoiceText());
                }
                if (questionId == 6 || questionId == 23) {
                    dto.setEducation(businessApplySubmit.getChoiceText());
                }
                if (questionId == 7 || questionId == 24) {
                    dto.setCollege(businessApplySubmit.getUserValue());
                }
                if (questionId == 8 || questionId == 25) {
                    dto.setLocation(businessApplySubmit.getUserValue());
                }
                if (questionId == 1 || questionId == 18) {
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

            if (openidList != null && CollectionUtils.isNotEmpty(openidList)) {
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

    private InterviewRecord convertInterview(InterviewDto interviewDto) {
        InterviewRecord interviewRecord = new InterviewRecord();
        String[] str = {"interviewTime"};
        BeanUtils.copyProperties(interviewDto, interviewRecord, str);
        interviewRecord.setInterviewTime(DateUtils.parseDateTimeToString(interviewDto.getInterviewTime()));

        return interviewRecord;
    }


    private String replaceNickname(String openid, String message) {
        Profile profile = accountService.getProfile(openid, false);
        String name = profile != null ? profile.getNickname() : "";
        return message.replace("{username}", name);
    }
}
