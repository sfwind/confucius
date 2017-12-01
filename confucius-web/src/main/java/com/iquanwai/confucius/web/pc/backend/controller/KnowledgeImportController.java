package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.backend.KnowledgeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;
import com.iquanwai.confucius.web.enums.KnowledgeEnums;
import com.iquanwai.confucius.web.pc.backend.dto.SimpleKnowledge;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pc/operation/knowledge")
public class KnowledgeImportController {
    @Autowired
    private KnowledgeService knowledgeService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping("/simple/{problemId}")
    public ResponseEntity<Map<String, Object>> getSimpleKnowledge(PCLoginUser loginUser,
                                                                  @PathVariable Integer problemId) {
        List<SimpleKnowledge> simpleKnowledges = knowledgeService.loadKnowledges(problemId).stream()
                .map(knowledge -> new SimpleKnowledge(knowledge.getId(), knowledge.getKnowledge()))
                .collect(Collectors.toList());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("选择知识点")
                .action("加载知识点")
                .memo(problemId.toString());
        operationLogService.log(operationLog);

        return WebUtils.result(simpleKnowledges);
    }


    @RequestMapping(value = "/get/{knowledgeId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadKnowledgeDetail(PCLoginUser loginUser, @PathVariable Integer knowledgeId) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().module("后台管理").function("知识点录入").action("加载已有知识点信息")
                .openid(loginUser.getOpenId());
        operationLogService.log(operationLog);

        Knowledge knowledge = knowledgeService.loadKnowledge(knowledgeId);
        if (knowledge != null) {
            return WebUtils.result(knowledge);
        } else {
            return WebUtils.error("当前知识点不存在，请重试");
        }
    }

    @RequestMapping(value = "/update/{problemId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateKnowledge(PCLoginUser loginUser,
                                                               @PathVariable Integer problemId,
                                                               @RequestBody Knowledge knowledge) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().module("后台管理").function("知识点录入").action("更新知识点")
                .openid(loginUser.getOpenId());
        operationLogService.log(operationLog);

        int result = knowledgeService.updateKnowledge(knowledge, problemId);
        if (result > 0) {
            return WebUtils.result(result);
        } else if(result == KnowledgeEnums.KNOWLEDG_Duplicate_ERROR.getCode()){
            return WebUtils.error(KnowledgeEnums.KNOWLEDG_Duplicate_ERROR.getMsg());
        }
        else{
            return WebUtils.error(KnowledgeEnums.UNKNOWN_ERROR.getMsg());
        }
    }

    @RequestMapping(value = "/query/knowledges",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> queryKnowledges(PCLoginUser loginUser){
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().module("后台管理").function("查询知识点").action("查询知识点")
                .openid(loginUser.getOpenId());

        operationLogService.log(operationLog);

        List<Knowledge> result = knowledgeService.queryAllKnowLedges();

        return WebUtils.result(result);
    }

}
