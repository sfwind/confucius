package com.iquanwai.confucius.web.course.controller;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.backend.MonthlyCampService;
import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.template.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.template.TemplateMessageService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQPublisher;
import com.iquanwai.confucius.web.course.dto.backend.*;
import com.iquanwai.confucius.web.pc.LoginUserService;
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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by justin on 16/10/8.
 */
@RestController
@RequestMapping("/b")
public class BackendController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private SignupService signupService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private CourseProgressService courseProgressService;
    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private MonthlyCampService monthlyCampService;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    private RabbitMQPublisher rabbitMQPublisher;


    @PostConstruct
    public void init() {
        rabbitMQPublisher = rabbitMQFactory.initFanoutPublisher(PayService.LOGIN_USER_RELOAD_TOPIC);
    }


    @RequestMapping(value = "/log", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> log(HttpServletRequest request, LoginUser loginUser, @RequestBody ErrorLogDto errorLogDto) {
        String data = errorLogDto.getResult();
        StringBuilder sb = new StringBuilder();
        if (data.length() > 700) {
            data = data.substring(0, 700);
        }
        sb.append("url:");
        sb.append(errorLogDto.getUrl());
        sb.append(";data:");
        sb.append(data);
        sb.append(";ip:");
        sb.append(request.getHeader("X-Forwarded-For"));
        sb.append(";browser:");
        sb.append(errorLogDto.getBrowser());
        sb.append(";cookie:");
        if (sb.length() < 1024) {
            String cookie = errorLogDto.getCookie();
            int remain = 1024 - sb.length();
            if (remain < cookie.length()) {
                cookie = cookie.substring(0, remain);
            }
            sb.append(cookie);
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser == null ? null : loginUser.getOpenId())
                .module("记录前端bug")
                .function("bug")
                .action("bug")
                .memo(sb.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping("/t")
    public ResponseEntity<Map<String, Object>> test(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        StringBuilder sb = new StringBuilder();
        for (Cookie cookie : cookies) {
            sb.append(cookie.getName())
                    .append("=")
                    .append(cookie.getValue())
                    .append(";");
        }
        String openid = oAuthService.openId(getAccessTokenFromCookie(sb.toString()));
        OperationLog operationLog = OperationLog.create().openid(openid)
                .module("测试")
                .function("测试")
                .action("获取cookie")
                .memo(sb.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping("/graduate/{classId}")
    public ResponseEntity<Map<String, Object>> graduate(@PathVariable("classId") Integer classId) {
        LOGGER.info("classId {} graduate start", classId);
        new Thread(() -> {
            try {
                courseProgressService.graduate(classId);
            } catch (Exception e) {
                LOGGER.error("触发毕业失败", e);
            }
        }).start();
        return WebUtils.result("正在运行中");
    }

    private static String getAccessTokenFromCookie(String cookieStr) {
        String[] cookies = cookieStr.split(";");
        String accessToken = "";
        for (String cookie : cookies) {
            if (cookie.startsWith(OAuthService.ACCESS_TOKEN_COOKIE_NAME + "=")) {
                accessToken = cookie.substring(OAuthService.ACCESS_TOKEN_COOKIE_NAME.length() + 1);
                break;
            }
        }
        return accessToken;
    }

    @RequestMapping(value = "/notice", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> notice(@RequestBody NoticeMsgDto noticeMsgDto) {
        Date date = new Date();
        new Thread(() -> {
            try {
                List<String> openids = noticeMsgDto.getOpenids();
                openids.forEach(openid -> {
                    TemplateMessage templateMessage = new TemplateMessage();
                    templateMessage.setTouser(openid);
                    templateMessage.setTemplate_id(noticeMsgDto.getMessageId());
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setData(data);
                    if (noticeMsgDto.getFirst() != null) {
                        String first = noticeMsgDto.getFirst();
                        if (first.contains("{username}")) {
                            first = replaceNickname(openid, first);
                        }
                        data.put("first", new TemplateMessage.Keyword(first));
                    }
                    if (noticeMsgDto.getKeyword1() != null) {
                        String keyword1 = noticeMsgDto.getKeyword1();
                        if (keyword1.contains("{username}")) {
                            keyword1 = replaceNickname(openid, keyword1);
                        }
                        data.put("keyword1", new TemplateMessage.Keyword(keyword1));
                    }
                    if (noticeMsgDto.getKeyword2() != null) {
                        String keyword2 = noticeMsgDto.getKeyword2();
                        if (keyword2.contains("{username}")) {
                            keyword2 = replaceNickname(openid, keyword2);
                        }
                        data.put("keyword2", new TemplateMessage.Keyword(keyword2));
                    }
                    if (noticeMsgDto.getKeyword3() != null) {
                        String keyword3 = noticeMsgDto.getKeyword3();
                        if (keyword3.contains("{username}")) {
                            keyword3 = replaceNickname(openid, keyword3);
                        }
                        data.put("keyword3", new TemplateMessage.Keyword(keyword3));
                    }
                    if (noticeMsgDto.getKeyword4() != null) {
                        String keyword4 = noticeMsgDto.getKeyword4();
                        if (keyword4.contains("{username}")) {
                            keyword4 = replaceNickname(openid, keyword4);
                        }
                        data.put("keyword4", new TemplateMessage.Keyword(keyword4));
                    }
                    if (noticeMsgDto.getRemark() != null) {
                        String remark = noticeMsgDto.getRemark();
                        if (remark.contains("{username}")) {
                            remark = replaceNickname(openid, remark);
                        }
                        data.put("remark", new TemplateMessage.Keyword(remark));
                    }
                    if (noticeMsgDto.getUrl() != null) {
                        templateMessage.setUrl(noticeMsgDto.getUrl());
                    }
                    templateMessageService.sendMessage(templateMessage);
                    messageService.logCustomerMessage(openid, date, noticeMsgDto.getComment());
                });
            } catch (Exception e) {
                LOGGER.error("发送通知失败", e);
            }
        }).start();
        return WebUtils.result("正在运行中");
    }

    @RequestMapping(value = "/system/msg", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> systemMsg(@RequestBody SystemMsgDto systemMsgDto) {
        Assert.notNull(systemMsgDto.getMessage(), "消息不能为空");
        new Thread(() -> {
            try {
                List<Integer> profileIds = systemMsgDto.getProfileIds();
                profileIds.forEach(profileId -> {
                    messageService.sendMessage(systemMsgDto.getMessage(), profileId.toString(),
                            MessageService.SYSTEM_MESSAGE, systemMsgDto.getUrl());

                });
            } catch (Exception e) {
                LOGGER.error("发送通知失败", e);
            }
        }).start();
        return WebUtils.result("正在运行中");
    }

    private String replaceNickname(String openid, String message) {
        Profile profile = accountService.getProfile(openid, false);
        String name = profile != null ? profile.getNickname() : "";
        return message.replace("{username}", name);
    }



    @RequestMapping(value = "/mark", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> mark(LoginUser loginUser, @RequestBody MarkDto markDto) {
        OperationLog operationLog = OperationLog.create().openid(loginUser == null ? null : loginUser.getOpenId())
                .module(markDto.getModule())
                .function(markDto.getFunction())
                .action(markDto.getAction())
                .memo(markDto.getMemo());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/login/users", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loginUsersList(@RequestParam(value = "qt") String qt) {
        LOGGER.info("qt:{},users:{}", qt, LoginUserService.pcLoginUserMap);
        Set<String> keys = LoginUserService.pcLoginUserMap.keySet();
        if (keys.contains(qt)) {
            SoftReference<PCLoginUser> pcLoginUserSoftReference = LoginUserService.pcLoginUserMap.get(qt);
            if (pcLoginUserSoftReference != null) {
                return WebUtils.result(pcLoginUserSoftReference.get());
            } else {
                return WebUtils.error("有cookie但是没有引用");
            }
        } else {
            return WebUtils.error("没有这个cookie");
        }
    }

    @RequestMapping(value = "/refresh/users", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> loginUsersList(@RequestBody RefreshLoginUserDto refreshLoginUserDto) {
        new Thread(() -> {
            try {
                List<String> openIds = refreshLoginUserDto.getOpenIds();
                openIds.forEach(openid -> {
                    try {
                        rabbitMQPublisher.publish(openid);
                        //防止队列阻塞
                        Thread.sleep(50);
                    } catch (Exception e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                    }
                });
            } catch (Exception e) {
                LOGGER.error("发送通知失败", e);
            }
        }).start();
        return WebUtils.result("正在运行中");
    }

    @RequestMapping(value = "/batch/open/camp")
    public ResponseEntity<Map<String, Object>> batchForceOpenMonthlyCamp(@RequestBody BatchOpenCourseDto batchOpenCourseDto) {
        OperationLog operationLog = OperationLog.create().module("后台功能").function("批量开始课程")
                .memo("月份:" + batchOpenCourseDto.getMonth()
                        + ", 小课Id:" + batchOpenCourseDto.getProblemId()
                        + ", 小课关闭日期:" + batchOpenCourseDto.getCloseDate());
        operationLogService.log(operationLog);

        Integer month = batchOpenCourseDto.getMonth();
        Integer problemId = batchOpenCourseDto.getProblemId();
        Date closeDate = batchOpenCourseDto.getCloseDate() == null ? DateUtils.afterDays(new Date(), 30) : batchOpenCourseDto.getCloseDate();


        boolean validation = monthlyCampService.validForceOpenCourse(month, problemId);
        if (validation) {
            Thread thread = new Thread(() -> monthlyCampService.batchForceOpenCourse(problemId, closeDate));
            thread.start();
            return WebUtils.result("开课进行中");
        } else {
            return WebUtils.error("开课校验失败");
        }
    }

}

