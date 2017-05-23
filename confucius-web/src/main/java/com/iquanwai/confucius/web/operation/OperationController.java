package com.iquanwai.confucius.web.operation;

import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.operation.OperationService;
import com.iquanwai.confucius.biz.po.Coupon;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by xfduan on 2017/5/20.
 */
@RestController
@RequestMapping(value = "/operation")
public class OperationController {

    @Autowired
    private OperationService operationService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping(value = "discount", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getDiscountValue(LoginUser loginUser) {
        // 日志记录
        OperationLog operationLog = new OperationLog().module("临时活动").function("获取折扣金额");
        operationLogService.log(operationLog);
        Integer validaCourseCount = operationService.getValidCourseCount(loginUser.getOpenId());
        if(validaCourseCount == 0) {
            return WebUtils.error(202, "无活动权限");
        }
        Coupon alreadyGetDiscountCoupon = operationService.alreadyGetDiscount(loginUser.getOpenId());
        if(alreadyGetDiscountCoupon != null) {
            if(alreadyGetDiscountCoupon.getExpiredDate().toString().equals(DateUtils.parseDateToString(ConfigUtils.getDiscountExpiredDate()))) {
                return WebUtils.error(201, alreadyGetDiscountCoupon.getAmount());
            } else {
                return WebUtils.result(alreadyGetDiscountCoupon.getAmount());
            }
        } else {
            Integer disCountValue = operationService.getDiscountValue(loginUser.getOpenId());
            return WebUtils.result(disCountValue);
        }
    }

    @RequestMapping(value = "discount/valid", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> validDiscount(LoginUser loginUser) {
        // 日志记录
        OperationLog operationLog = new OperationLog().module("临时活动").function("生效折扣金额");
        operationLogService.log(operationLog);
        Integer validCount = operationService.validDiscount(loginUser.getOpenId());
        if (validCount > 0) {
            return WebUtils.success();
        } else {
            return WebUtils.error("系统错误");
        }
    }

}
