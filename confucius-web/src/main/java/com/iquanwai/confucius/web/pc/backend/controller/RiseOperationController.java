package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.backend.OperationManagementService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPractice;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPracticeDiscuss;
import com.iquanwai.confucius.biz.util.page.Page;
import com.iquanwai.confucius.web.pc.backend.dto.DiscussDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @RequestMapping("/application/submit/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadApplicationSubmit(PCLoginUser loginUser,
                                                                    @PathVariable Integer applicationId,
                                                                    @ModelAttribute Page page) {
        List<ApplicationSubmit> applicationSubmitList = operationManagementService.loadApplicationSubmit(applicationId, page);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("应用训练提交")
                .action("加载应用训练提交")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);

        return WebUtils.result(applicationSubmitList);
    }


    @RequestMapping("/hot/warmup/{practiceId}")
    public ResponseEntity<Map<String, Object>> getHotPracticeDiscuss(PCLoginUser loginUser,
                                                                     @PathVariable Integer practiceId) {
        List<WarmupPractice> warmupPractices = operationManagementService.getLastTwoDayActivePractice();

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("理解训练讨论区")
                .action("加载最热的理解训练")
                .memo(practiceId.toString());
        operationLogService.log(operationLog);

        return WebUtils.result(warmupPractices);
    }

    @RequestMapping("/warmup/discuss/{practiceId}")
    public ResponseEntity<Map<String, Object>> getPracticeDiscuss(PCLoginUser loginUser,
                                                                    @PathVariable Integer practiceId,
                                                                    @ModelAttribute Page page) {
        List<WarmupPracticeDiscuss> warmupPracticeDiscusses = operationManagementService.getWarmupDiscuss(practiceId, page);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("理解训练讨论区")
                .action("加载理解训练讨论")
                .memo(practiceId.toString());
        operationLogService.log(operationLog);

        return WebUtils.result(warmupPracticeDiscusses);
    }


    @RequestMapping("/reply/discuss/{discussId}")
    public ResponseEntity<Map<String, Object>> replyDiscuss(PCLoginUser loginUser,
                                                                  @RequestBody DiscussDto discussDto) {
        if(discussDto.getComment()==null || discussDto.getComment().length()>300){
            LOGGER.error("{} 理解训练讨论字数过长", loginUser.getOpenId());
            return WebUtils.result("您提交的讨论字数过长");
        }

        operationManagementService.discuss(loginUser.getOpenId(), discussDto.getWarmupPracticeId(),
                discussDto.getComment(), discussDto.getRepliedId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("理解训练")
                .action("讨论")
                .memo(discussDto.getWarmupPracticeId().toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

}
