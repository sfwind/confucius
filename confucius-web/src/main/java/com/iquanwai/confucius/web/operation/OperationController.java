package com.iquanwai.confucius.web.operation;

import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.operation.OperationService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
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
        OperationLog operationLog = new OperationLog().module("临时活动").function("获取折扣金额")
                .memo(loginUser.getOpenId());
        operationLogService.log(operationLog);

        Boolean alreadyGetDisCount = operationService.alreadyGetDiscount(loginUser.getOpenId());
        if (!alreadyGetDisCount) {
            Integer disCountValue = operationService.getDiscountValue(loginUser.getOpenId());
            if (disCountValue == 0) {
                return WebUtils.error(201,"用户并未购买过任何付费产品");
            } else {
                return WebUtils.result(disCountValue);
            }
        } else {
            return WebUtils.error(202,"您已参加过此次活动");
        }
    }

}
