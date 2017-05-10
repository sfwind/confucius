package com.iquanwai.confucius.web.course.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.po.systematism.CourseOrder;
import com.iquanwai.confucius.web.course.dto.backend.ErrorLogDto;
import com.iquanwai.confucius.web.course.dto.backend.MarkDto;
import com.iquanwai.confucius.web.course.dto.backend.NoticeMsgDto;
import com.iquanwai.confucius.web.course.dto.backend.SignupClassDto;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/10/8.
 */
@RestController
@RequestMapping("/b")
public class BackendController {
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

    private Logger LOGGER = LoggerFactory.getLogger(getClass());


    @RequestMapping("/entry/{orderId}")
    public ResponseEntity<Map<String, Object>> entry(@PathVariable("orderId") String orderId){
        String result;
        CourseOrder courseOrder = signupService.getOrder(orderId);
        if(courseOrder !=null){
            if(courseOrder.getEntry()){
                String memberId = signupService.entry(orderId);
                result = "报名成功, 学号是"+memberId;
            }else{
                result = "尚未付款";
            }
        }else{
            result = "订单不存在";
        }

        return WebUtils.result(result);
    }

    @RequestMapping(value = "/log", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> log(HttpServletRequest request,LoginUser loginUser,@RequestBody ErrorLogDto errorLogDto){
        String data = errorLogDto.getResult();
        StringBuilder sb = new StringBuilder();
        if(data.length()>700){
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
    public ResponseEntity<Map<String, Object>> test(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        StringBuilder sb = new StringBuilder();
        for(Cookie cookie:cookies){
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
    public ResponseEntity<Map<String, Object>> graduate(@PathVariable("classId") Integer classId){
        LOGGER.info("classId {} graduate start", classId);
        new Thread(() -> {
            try {
                courseProgressService.graduate(classId);
            }catch (Exception e){
                LOGGER.error("触发毕业失败", e);
            }
        }).start();
        return WebUtils.result("正在运行中");
    }

    private static String getAccessTokenFromCookie(String cookieStr){
        String[] cookies = cookieStr.split(";");
        String accessToken ="";
        for(String cookie:cookies){
            if(cookie.startsWith(OAuthService.ACCESS_TOKEN_COOKIE_NAME+"=")){
                accessToken = cookie.substring(OAuthService.ACCESS_TOKEN_COOKIE_NAME.length()+1);
                break;
            }
        }
        return accessToken;
    }

    @RequestMapping(value = "/notice", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> notice(@RequestBody NoticeMsgDto noticeMsgDto){
        new Thread(() -> {
            try {
                List<String> openids = noticeMsgDto.getOpenids();
                openids.stream().forEach(openid -> {
                    TemplateMessage templateMessage = new TemplateMessage();
                    templateMessage.setTouser(openid);
                    templateMessage.setTemplate_id(noticeMsgDto.getMessageId());
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setData(data);
                    if(noticeMsgDto.getFirst()!=null) {
                        String first = noticeMsgDto.getFirst();
                        if(first.contains("{username}")){
                            first = replaceNickname(openid, first);
                        }
                        data.put("first", new TemplateMessage.Keyword(first));
                    }
                    if(noticeMsgDto.getKeyword1()!=null){
                        data.put("keyword1", new TemplateMessage.Keyword(noticeMsgDto.getKeyword1()));
                    }
                    if(noticeMsgDto.getKeyword2()!=null) {
                        data.put("keyword2", new TemplateMessage.Keyword(noticeMsgDto.getKeyword2()));
                    }
                    if(noticeMsgDto.getKeyword3()!=null) {
                        data.put("keyword3", new TemplateMessage.Keyword(noticeMsgDto.getKeyword3()));
                    }
                    if(noticeMsgDto.getRemark()!=null) {
                        String remark = noticeMsgDto.getRemark();
                        if(remark.contains("{username}")){
                            remark = replaceNickname(openid, remark);
                        }
                        data.put("remark", new TemplateMessage.Keyword(remark));
                    }
                    if(noticeMsgDto.getUrl()!=null){
                        templateMessage.setUrl(noticeMsgDto.getUrl());
                    }
                    templateMessageService.sendMessage(templateMessage);

                });
            }catch (Exception e){
                LOGGER.error("发送通知失败", e);
            }
        }).start();
        return WebUtils.result("正在运行中");
    }

    private String replaceNickname(String openid, String message) {
        Profile profile = accountService.getProfile(openid, false);
        String name = profile!=null?profile.getNickname():"";
        return message.replace("{username}", name);
    }

    @RequestMapping(value = "/info/signup", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getSignUpInfo(){
        Map<Integer, Integer> remindingCount = signupService.getRemindingCount();
        // 查询各班已报名人数
        List<SignupClassDto> list = Lists.newArrayList();
        remindingCount.keySet().forEach(item->{
            List<ClassMember> members = courseProgressService.loadClassMembers(item);
            SignupClassDto dto = new SignupClassDto();
            dto.setId(item);
            dto.setRemainingCount(remindingCount.get(item));
            dto.setEntryCount(members.size());
            list.add(dto);
        });
        return WebUtils.result(list);
    }

    @RequestMapping(value = "/info/signup",method=RequestMethod.GET,params = "rise")
    public ResponseEntity<Map<String,Object>> getRiseInfo(){
        return WebUtils.result(signupService.getRiseRemindingCount());
    }

    @RequestMapping(value = "/mark", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> mark(LoginUser loginUser,@RequestBody MarkDto markDto) {
        OperationLog operationLog = OperationLog.create().openid(loginUser == null ? null : loginUser.getOpenId())
                .module(markDto.getModule())
                .function(markDto.getFunction())
                .action(markDto.getAction())
                .memo(markDto.getMemo());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }
}
