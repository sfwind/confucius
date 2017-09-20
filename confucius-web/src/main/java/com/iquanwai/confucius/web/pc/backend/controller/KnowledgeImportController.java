package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.backend.KnowledgeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
import com.iquanwai.confucius.web.pc.backend.dto.KnowledgeImportDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pc/operation/knowledge")
public class KnowledgeImportController {

    @Autowired
    private KnowledgeService knowledgeImportService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping(value = "/get/problem", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadEditableProblem(PCLoginUser loginUser) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().module("后台管理").function("知识点录入").action("加载小课基本信息")
                .openid(loginUser.getOpenId());
        operationLogService.log(operationLog);

        Problem problem = knowledgeImportService.loadEditableProblem();
        List<ProblemSchedule> schedules = knowledgeImportService.loadEditableProblemSchedules();

        if (problem != null) {
            KnowledgeImportDto dto = new KnowledgeImportDto();
            dto.setId(problem.getId());
            dto.setProblem(problem.getProblem());
            dto.setSchedules(schedules);
            return WebUtils.result(dto);
        } else {
            return WebUtils.error("当前无可修改小课");
        }
    }

    @RequestMapping(value = "/get/knowledge/{knowledgeId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadKnowledgeDetail(PCLoginUser loginUser, @PathVariable Integer knowledgeId) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().module("后台管理").function("知识点录入").action("加载已有知识点信息")
                .openid(loginUser.getOpenId());
        operationLogService.log(operationLog);

        Knowledge knowledge = knowledgeImportService.loadKnowledge(knowledgeId);
        if (knowledge != null) {
            return WebUtils.result(knowledge);
        } else {
            return WebUtils.error("当前知识点不存在，请重试");
        }
    }

    @RequestMapping(value = "/post/add/chapter/{chapter}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addNewChapter(PCLoginUser loginUser, @PathVariable Integer chapter) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().module("后台管理").function("知识点录入").action("新增章节")
                .openid(loginUser.getOpenId());
        operationLogService.log(operationLog);

        int result = knowledgeImportService.addNewChapter(chapter);
        if (result > 0) {
            return WebUtils.success();
        } else {
            return WebUtils.error("章节插入失败，请重试");
        }
    }

    @RequestMapping(value = "/post/add/section/{chapter}/{section}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addNewSection(PCLoginUser loginUser, @PathVariable Integer chapter, @PathVariable Integer section) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().module("后台管理").function("知识点录入").action("新增小节")
                .openid(loginUser.getOpenId());
        operationLogService.log(operationLog);

        int result = knowledgeImportService.addNewSection(chapter, section);
        if (result > 0) {
            return WebUtils.success();
        } else {
            return WebUtils.error("小节插入失败，请重试");
        }
    }

    @RequestMapping(value = "/post/update/knowledge", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateKnowledge(PCLoginUser loginUser, @RequestBody Knowledge knowledge) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().module("后台管理").function("知识点录入").action("更新知识点")
                .openid(loginUser.getOpenId());
        operationLogService.log(operationLog);

        int result = knowledgeImportService.updateKnowledge(knowledge);
        if (result > 0) {
            return WebUtils.success();
        } else {
            return WebUtils.error("更新失败");
        }
    }

}
