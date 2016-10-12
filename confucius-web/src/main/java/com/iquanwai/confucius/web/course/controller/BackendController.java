package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.domain.course.operational.OperationalService;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.po.CourseOrder;
import com.iquanwai.confucius.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        String result = "";
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
}
