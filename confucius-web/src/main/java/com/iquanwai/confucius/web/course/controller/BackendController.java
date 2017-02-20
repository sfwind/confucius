package com.iquanwai.confucius.web.course.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.course.operational.OperationalService;
import com.iquanwai.confucius.biz.domain.course.operational.PromoCodeService;
import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.confucius.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.PromoCode;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.po.systematism.CourseOrder;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.course.dto.backend.ErrorLogDto;
import com.iquanwai.confucius.web.course.dto.backend.NoticeMsgDto;
import com.iquanwai.confucius.web.course.dto.backend.SignupClassDto;
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
    private OperationalService operationalService;
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
    private PromoCodeService promoCodeService;

    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @RequestMapping("/assign/angel/{classId}")
    public ResponseEntity<Map<String, Object>> submit(@PathVariable("classId") Integer classId){
        boolean result = operationalService.angelAssign(classId);

        return WebUtils.result(result);
    }

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
    public ResponseEntity<Map<String, Object>> log(@RequestBody ErrorLogDto errorLogDto){
        String data = errorLogDto.getResult();
        if(data.length()>1024){
            data = data.substring(0, 1024);
        }
        String cookieStr= errorLogDto.getCookie();

        String openid = oAuthService.openId(getAccessTokenFromCookie(cookieStr));
        OperationLog operationLog = OperationLog.create().openid(openid)
                .module("bug")
                .function("bug")
                .action("记录前端bug")
                .memo(data);
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
                    templateMessage.setTemplate_id(ConfigUtils.incompleteTaskMsgKey());
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setData(data);
                    if(noticeMsgDto.getFirst()!=null) {
                        data.put("first", new TemplateMessage.Keyword(noticeMsgDto.getFirst()));
                    }
                    data.put("keyword1", new TemplateMessage.Keyword(noticeMsgDto.getTask()));
                    data.put("keyword2", new TemplateMessage.Keyword("hin高"));
                    if(noticeMsgDto.getRemark()!=null) {
                        data.put("remark", new TemplateMessage.Keyword(noticeMsgDto.getRemark()));
                    }
                    templateMessageService.sendMessage(templateMessage);

                });
            }catch (Exception e){
                LOGGER.error("发送通知失败", e);
            }
        }).start();
        return WebUtils.result("正在运行中");
    }

    @RequestMapping(value = "/promoCode/{activityCode}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> sendPromoInfo(@PathVariable String activityCode){
        new Thread(() -> {
            try {
                List<PromoCode> promoCodes = promoCodeService.getPromoCodes(activityCode);
                promoCodes.stream().forEach(promoCode -> {
                    TemplateMessage templateMessage = new TemplateMessage();
                    templateMessage.setTouser(promoCode.getOwner());
                    templateMessage.setTemplate_id(ConfigUtils.activityStartMsgKey());
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    data.put("first", new TemplateMessage.Keyword("老学员福利，错过一次，再等N年！\n" +
                            "圈圈亲设，两堂受益一生的求职&职业规划课\n"+
                            "分享优惠码，还可免费听课程！"));
                    data.put("keyword1", new TemplateMessage.Keyword("这个春天，一起来重新学习职业发展！"));
                    data.put("keyword2", new TemplateMessage.Keyword("3月1日 ~3月31日"));
                    data.put("remark", new TemplateMessage.Keyword("您的优惠码为:"+promoCode.getCode(), TemplateMessage.BLACK));
                    templateMessage.setData(data);
//                    templateMessage.setUrl("");

                    templateMessageService.sendMessage(templateMessage);
                });
            } catch (Exception e) {
                LOGGER.error("发送通知失败", e);
            }
        }).start();
        return WebUtils.result("正在运行中");
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




}
