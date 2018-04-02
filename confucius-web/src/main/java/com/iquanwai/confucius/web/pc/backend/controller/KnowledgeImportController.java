package com.iquanwai.confucius.web.pc.backend.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.backend.KnowledgeService;
import com.iquanwai.confucius.biz.domain.backend.ProblemService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;
import com.iquanwai.confucius.biz.po.fragmentation.KnowledgeDiscuss;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.web.enums.KnowledgeEnums;
import com.iquanwai.confucius.web.pc.backend.dto.KnowledgeDiscussDto;
import com.iquanwai.confucius.web.pc.backend.dto.ProblemKnowledgesDto;
import com.iquanwai.confucius.web.pc.backend.dto.SimpleKnowledge;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.resolver.UnionUser;
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
    @Autowired
    private ProblemService problemService;
    @Autowired
    private AccountService accountService;

    @RequestMapping("/simple/{problemId}")
    public ResponseEntity<Map<String, Object>> getSimpleKnowledge(PCLoginUser loginUser, @PathVariable Integer problemId) {
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
    public ResponseEntity<Map<String, Object>> updateKnowledge(PCLoginUser loginUser, @PathVariable Integer problemId, @RequestBody Knowledge knowledge) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().module("后台管理").function("知识点录入").action("更新知识点")
                .openid(loginUser.getOpenId());
        operationLogService.log(operationLog);

        int result = knowledgeService.updateKnowledge(knowledge, problemId);
        if (result > 0) {
            return WebUtils.result(result);
        } else if (result == KnowledgeEnums.KNOWLEDG_Duplicate_ERROR.getCode()) {
            return WebUtils.error(KnowledgeEnums.KNOWLEDG_Duplicate_ERROR.getMsg());
        } else {
            return WebUtils.error(KnowledgeEnums.UNKNOWN_ERROR.getMsg());
        }
    }

    @RequestMapping(value = "/query/knowledges", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> queryKnowledges(PCLoginUser loginUser) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().module("后台管理").function("查询知识点").action("查询知识点")
                .openid(loginUser.getOpenId());

        operationLogService.log(operationLog);

        List<Knowledge> result = knowledgeService.queryAllKnowLedges();

        return WebUtils.result(result);
    }

    @RequestMapping(value = "/load/knowledges", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadKnowledgesGroupByProblem(PCLoginUser loginUser) {
        Assert.notNull(loginUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理").function("巩固练习新增").action("加载问题与知识点");
        operationLogService.log(operationLog);
        List<Problem> problems = problemService.loadProblems();
        List<ProblemSchedule> knowledges = knowledgeService.loadKnowledgesGroupByProblem();
        if (problems != null && knowledges != null) {
            ProblemKnowledgesDto dto = new ProblemKnowledgesDto();
            dto.setProblems(problems);
            dto.setKnowledges(knowledges);
            return WebUtils.result(dto);
        }
        return WebUtils.error("未找到课程与知识点关联信息");
    }

    @RequestMapping(value = "/load/problem/knowledges", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadKnowledgesByProblemId(UnionUser unionUser, @RequestParam("problemId") Integer problemId) {
        return WebUtils.result(knowledgeService.loadKnowledgesByProblemId(problemId));
    }

    @RequestMapping(value = "/load/discuss")
    public ResponseEntity<Map<String, Object>> loadKnowledgeDiscuss(UnionUser unionUser, @RequestParam("knowledgeId") Integer knowledgeId) {
        List<KnowledgeDiscuss> knowledgeDiscusses = knowledgeService.loadKnowledgeDiscussByKnowledgeId(knowledgeId);
        List<Integer> profileIds = knowledgeDiscusses.stream().map(KnowledgeDiscuss::getProfileId).collect(Collectors.toList());
        List<Profile> profiles = accountService.getProfiles(profileIds);
        Map<Integer, Profile> profileMap = profiles.stream().collect(Collectors.toMap(Profile::getId, profile -> profile, (key1, key2) -> key1));

        List<KnowledgeDiscussDto> knowledgeDiscussDtos = Lists.newArrayList();
        knowledgeDiscusses.forEach(knowledgeDiscuss -> {
            Profile profile = profileMap.get(knowledgeDiscuss.getProfileId());
            if (profile != null) {
                KnowledgeDiscussDto dto = new KnowledgeDiscussDto();
                dto.setId(knowledgeDiscuss.getId());
                dto.setHeadImgUrl(profile.getHeadimgurl());
                dto.setNickName(profile.getRealName());
                dto.setPublishTime(DateUtils.parseDateToString(knowledgeDiscuss.getAddTime()));
                dto.setComment(knowledgeDiscuss.getComment());
                dto.setPriority(knowledgeDiscuss.getPriority());
                knowledgeDiscussDtos.add(dto);
            }
        });
        return WebUtils.result(knowledgeDiscussDtos);
    }

    @RequestMapping(value = "/vote/discuss", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> voteKnowledgeDiscuss(UnionUser unionUser, @RequestParam("discussId") Integer discussId, @RequestParam("priority") Boolean priority) {
        int result = knowledgeService.updatePriority(discussId, priority);
        if (result > 0) {
            return WebUtils.success();
        } else {
            return WebUtils.error("当前知识点评论不存在");
        }
    }

}
