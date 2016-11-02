package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.domain.course.operational.OperationalService;
import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.CourseOrder;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
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

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @RequestMapping("/assign/angel/{classId}")
    public ResponseEntity<Map<String, Object>> submit(@PathVariable("classId") Integer classId){
        boolean result = false;
        try {
            result = operationalService.angelAssign(classId);
        }catch (Exception e){
            LOGGER.error("分配天使失败", e);
        }

        return WebUtils.result(result);
    }

    @RequestMapping("/entry/{orderId}")
    public ResponseEntity<Map<String, Object>> entry(@PathVariable("orderId") String orderId){
        String result;
        try {
            CourseOrder courseOrder = signupService.getCourseOrder(orderId);
            if(courseOrder!=null){
                if(courseOrder.getStatus()==1){
                    String memberId = signupService.entry(courseOrder.getCourseId(),
                            courseOrder.getClassId(),
                            courseOrder.getOpenid());
                    result = "报名成功, 学号是"+memberId;
                }else{
                    result = "尚未付款";
                }
            }else{
                result = "订单不存在";
            }
        }catch (Exception e){
            result = "出现异常，报名失败";
            LOGGER.error(result, e);
        }

        return WebUtils.result(result);
    }

    @RequestMapping("/log")
    public ResponseEntity<Map<String, Object>> log(){
        return WebUtils.success();
    }

    @RequestMapping("/t")
    public ResponseEntity<Map<String, Object>> test(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        StringBuilder sb = new StringBuilder();
        for(Cookie cookie:cookies){
            sb.append(cookie.getName())
                    .append(":")
                    .append(cookie.getValue())
                    .append(",");
        }
        OperationLog operationLog = OperationLog.create().openid("")
                .module("测试")
                .function("测试")
                .action("获取cookie")
                .memo(sb.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping("/graduate/{classId}")
    public ResponseEntity<Map<String, Object>> graduate(@PathVariable("classId") Integer classId){
        try{
            LOGGER.info("{} graduate start", classId);
            new Thread(() -> {
                courseProgressService.graduate(classId);
            }).start();
            return WebUtils.success();
        }catch (Exception e){
            LOGGER.error("触发毕业失败", e);
            return WebUtils.error("触发毕业失败");
        }
    }
}
