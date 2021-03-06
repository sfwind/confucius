package com.iquanwai.confucius.web.backend;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.customer.CustomerMessageService;
import com.iquanwai.confucius.biz.domain.weixin.message.template.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.template.TemplateMessageService;
import com.iquanwai.confucius.biz.domain.weixin.pay.PayService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.ThreadPool;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.confucius.biz.util.rabbitmq.RabbitMQPublisher;
import com.iquanwai.confucius.web.backend.dto.CustomerMsgDto;
import com.iquanwai.confucius.web.backend.dto.ErrorLogDto;
import com.iquanwai.confucius.web.backend.dto.MarkDto;
import com.iquanwai.confucius.web.backend.dto.NoticeMsgDto;
import com.iquanwai.confucius.web.backend.dto.ProfileSetDto;
import com.iquanwai.confucius.web.backend.dto.RefreshLoginUserDto;
import com.iquanwai.confucius.web.backend.dto.SystemMsgDto;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.HandleStringUtils;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/10/8.
 */
@RestController
@RequestMapping("/b")
public class BackendController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private SignupService signupService;

    private RabbitMQPublisher userReloadPublisher;


    @PostConstruct
    public void init() {
        userReloadPublisher = rabbitMQFactory.initFanoutPublisher(PayService.LOGIN_USER_RELOAD_TOPIC);
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

    @RequestMapping(value = "/notice", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> notice(@RequestBody NoticeMsgDto noticeMsgDto) {
        String source = noticeMsgDto.getSource();
        if (source == null || HandleStringUtils.hasChinese(source)) {
            return WebUtils.error("source是必填字段,值不能含中文!");
        }
        ThreadPool.execute(() -> {
            try {
                // 所有待发人员名单
                List<String> openIds = noticeMsgDto.getOpenids();
                List<String> excludeOpenIds = noticeMsgDto.getExcludes() == null ? Lists.newArrayList() : noticeMsgDto.getExcludes();

                // 获取黑名单人员
                List<String> blackListOpenIds = accountService.loadBlackListOpenIds();
                // 过滤调黑名单人员
                openIds = openIds.stream()
                        .distinct()
                        .filter(openId -> !blackListOpenIds.contains(openId))
                        .filter(openId -> !excludeOpenIds.contains(openId))
                        .collect(Collectors.toList());

                openIds.forEach(openid -> {
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
                        if (!StringUtils.isEmpty(noticeMsgDto.getRemarkColor())) {
                            data.put("remark", new TemplateMessage.Keyword(remark, noticeMsgDto.getRemarkColor()));
                        } else {
                            data.put("remark", new TemplateMessage.Keyword(remark));
                        }
                    }
                    if (noticeMsgDto.getUrl() != null) {
                        templateMessage.setUrl(noticeMsgDto.getUrl());
                    }
                    templateMessage.setComment(noticeMsgDto.getComment());

                    Boolean forcePush = noticeMsgDto.getForcePush();
                    // forcePush： 强制推送  forwardlyPush：主动推送
                    // 非主动推送不会进行校验
                    templateMessageService.sendMessage(templateMessage, forcePush == null || !forcePush,
                            noticeMsgDto.getSource());
                });
            } catch (Exception e) {
                logger.error("发送通知失败", e);
            }
        });
        return WebUtils.result("正在运行中");
    }

    @RequestMapping(value = "/system/msg", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> systemMsg(@RequestBody SystemMsgDto systemMsgDto) {
        Assert.notNull(systemMsgDto.getMessage(), "消息不能为空");
        ThreadPool.execute(() -> {
            try {
                List<Integer> profileIds = systemMsgDto.getProfileIds();
                profileIds.forEach(profileId -> messageService.sendMessage(systemMsgDto.getMessage(),
                        profileId.toString(), MessageService.SYSTEM_MESSAGE, systemMsgDto.getUrl()));
            } catch (Exception e) {
                logger.error("发送通知失败", e);
            }
        });
        return WebUtils.result("正在运行中");
    }

    @RequestMapping(value = "/customer/msg", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> customerMsg(@RequestBody CustomerMsgDto customerMsgDto) {
        Assert.notNull(customerMsgDto.getMessage(), "消息不能为空");
        ThreadPool.execute(() -> {
            try {
                List<String> openIds = customerMsgDto.getOpenids();
                String message = customerMsgDto.getMessage();
                openIds.forEach(openid -> {
                    String realMessage = replaceNickname(openid, message);
                    customerMessageService.sendCustomerMessage(openid, realMessage,
                            Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                });
            } catch (Exception e) {
                logger.error("发送通知失败", e);
            }
        });
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

    @RequestMapping(value = "/refresh/users", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> loginUsersList(@RequestBody RefreshLoginUserDto refreshLoginUserDto) {
        ThreadPool.execute(() -> {
            try {
                List<String> unionIds = refreshLoginUserDto.getUnionIds();
                unionIds.forEach(unionId -> {
                    try {
                        userReloadPublisher.publish(unionId);
                        //防止队列阻塞
                        Thread.sleep(50);
                    } catch (Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                });
            } catch (Exception e) {
                logger.error("发送通知失败", e);
            }
        });
        return WebUtils.result("正在运行中");
    }

    @RequestMapping(value = "/sa/profile/batch", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> batchProfileSet(@RequestBody ProfileSetDto profileSetDto) {
        if (CollectionUtils.isEmpty(profileSetDto.getProfiles())) {
            return WebUtils.error("必须输入用户列表 profiles");
        }
        if (profileSetDto.getKey() == null) {
            return WebUtils.error("必须输入key");
        }
        if (profileSetDto.getValue() == null) {
            return WebUtils.error("必须输入value");
        }
        profileSetDto.getProfiles().forEach(profileId -> operationLogService.profileSet(profileId, profileSetDto.getKey(), profileSetDto.getValue()));
        return WebUtils.success();
    }

    @RequestMapping(value = "/remain/number", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> setRemainPerson(@RequestParam(value = "memberTypeId") Integer memberTypeId,
                                                               @RequestParam(value = "close", required = false) Boolean close,
                                                               @RequestParam(value = "number", required = false) Integer number) {
        if (close != null && close) {
            signupService.changeRemainNumber(null, memberTypeId);
        } else {
            signupService.changeRemainNumber(number, memberTypeId);
        }
        return WebUtils.success();
    }

    @RequestMapping(value = "/sa/profile/update", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> bachUpdateProfile(@RequestBody ProfileSetDto profileSetDto) {
        if (CollectionUtils.isEmpty(profileSetDto.getProfiles())) {
            return WebUtils.error("必须输入用户列表 profiles");
        }

        operationLogService.refreshProfiles(profileSetDto.getProfiles());
        return WebUtils.success();
    }

}

