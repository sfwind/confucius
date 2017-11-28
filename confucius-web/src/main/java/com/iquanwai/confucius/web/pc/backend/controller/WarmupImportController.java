package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.backend.OperationManagementService;
import com.iquanwai.confucius.biz.domain.backend.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupChoice;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPractice;
import com.iquanwai.confucius.web.pc.backend.dto.ProblemKnowledgesDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 2017/9/15.
 */
@RestController
@RequestMapping("/pc/operation")
public class WarmupImportController {
    @Autowired
    private OperationManagementService operationManagementService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private PracticeService practiceService;

    @RequestMapping("/warmup/list/{problemId}")
    public ResponseEntity<Map<String, Object>> getProblemWarmupPractice(PCLoginUser loginUser,
                                                                        @PathVariable Integer problemId) {
        List<WarmupPractice> warmupPractices = operationManagementService.getPracticeByProblemId(problemId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习编辑")
                .action("加载小课的巩固练习");
        operationLogService.log(operationLog);

        return WebUtils.result(warmupPractices);
    }

    @RequestMapping(value = "/warmup/save", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> savePractice(PCLoginUser loginUser,
                                                            @RequestBody WarmupPractice warmupPractice) {

        operationManagementService.save(warmupPractice);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理")
                .function("巩固练习编辑")
                .action("保存巩固练习")
                .memo(warmupPractice.getId() + "");
        operationLogService.log(operationLog);

        return WebUtils.success();
    }

    @RequestMapping("/warmup/next/{problemId}/{practiceId}")
    public ResponseEntity<Map<String, Object>> getNextPractice(PCLoginUser loginUser,
                                                               @PathVariable Integer problemId,
                                                               @PathVariable Integer practiceId) {

        WarmupPractice warmupPractice = operationManagementService.getNextPractice(problemId, practiceId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理")
                .function("巩固练习编辑")
                .action("加载下一巩固练习");
        operationLogService.log(operationLog);

        return WebUtils.result(warmupPractice);
    }

    @RequestMapping(value = "/warmup/load/knowledges", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadKnowledgesGroupByProblem(PCLoginUser loginUser) {
        Assert.notNull(loginUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理").function("巩固练习新增").action("加载问题与知识点");
        operationLogService.log(operationLog);
        List<Problem> problems = problemService.loadProblems();
        List<ProblemSchedule> knowledges = operationManagementService.loadKnowledgesGroupByProblem();
        if(problems != null && knowledges != null) {
            ProblemKnowledgesDto dto = new ProblemKnowledgesDto();
            dto.setProblems(problems);
            dto.setKnowledges(knowledges);
            return WebUtils.result(dto);
        }
        return WebUtils.error("未找到课程与知识点关联信息");
    }

    @RequestMapping(value = "/warmup/insert/practice", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> insertWarmupPractice(PCLoginUser loginUser, @RequestBody WarmupPractice warmupPractice) {
        Assert.notNull(loginUser, "用户不能为空");
        List<WarmupChoice> warmupChoices = warmupPractice.getChoices();
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理")
                .function("巩固练习新增")
                .action("新增巩固练习");
        operationLogService.log(operationLog);
        // 删除过期巩固练习
        // practiceService.delWarmupPracticeByPracticeUid(warmupPractice.getPracticeUid());
        // 根据 PracticeUid 获取 WarmupPractice 的总数
        Integer practiceCnt =  practiceService.loadWarmupPracticeCntByPracticeUid(warmupPractice.getPracticeUid());
        if(practiceCnt > 0) {
            return WebUtils.error("当前 UID 选择题已存在，请联系管理员重试");
        }
        Integer knowledgeId = practiceService.insertWarmupPractice(warmupPractice);
        if(knowledgeId <= 0) {
            return WebUtils.error("选择题数据插入失败，请及时练习管理员");
        } else {
            practiceService.insertWarmupChoice(knowledgeId, warmupChoices);
        }
        return WebUtils.success();
    }

    @RequestMapping(value = "/warmup/load/problem/{practiceUid}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadProblemByPracticeUid(PCLoginUser loginUser, @PathVariable String practiceUid) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理")
                .function("巩固练习新增")
                .action("获取默认小课信息");
        operationLogService.log(operationLog);
        WarmupPractice warmupPractice = practiceService.loadWarmupPracticeByPracticeUid(practiceUid);
        if(warmupPractice != null) {
            return WebUtils.result(warmupPractice);
        } else  {
            return WebUtils.error("未找到对应课程数据");
        }
    }
}
