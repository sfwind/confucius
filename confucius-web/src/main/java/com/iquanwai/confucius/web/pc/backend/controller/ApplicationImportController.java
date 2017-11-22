package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.fragmentation.practice.ApplicationService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationPractice;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 应用题导入controller
 * @author iquanwai_yang
 */
@RestController
@RequestMapping("/pc/operation")
public class ApplicationImportController {

    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private ApplicationService applicationService;

    @RequestMapping(value = "/application/insert/practice",method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> insertApplicationPractice(PCLoginUser loginUser, @RequestBody ApplicationPractice applicationPractice){
        Assert.notNull(loginUser,"用户不能为空");

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理")
                .function("应用题新增")
                .action("新增应用题");
        operationLogService.log(operationLog);

        int practiceId = applicationService.insertApplicationPractice(applicationPractice);
        if(practiceId <= 0){
            return WebUtils.error("应用题数据插入失败，请及时联系管理员");
        }
        return WebUtils.success();
    }


}
