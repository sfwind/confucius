package com.iquanwai.confucius.web.pc.controller.fragmentation;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengePractice;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.resolver.PCLoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.pc.dto.RedirectRouteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2016/12/29.
 */
@RestController
@RequestMapping("/pc/fragment/problem")
public class ProblemController {
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private PlanService planService;
    @Autowired
    private OperationLogService operationLogService;

    /**
     * 用户点击了问题之后去哪里
     *
     * @param loginUser user
     */
    @RequestMapping("where")
    public ResponseEntity<Map<String, Object>> problemGoWhere(PCLoginUser loginUser, @RequestParam Integer problemId) {
        // 获得用户的计划
        String openId = loginUser.getOpenId();
        List<ImprovementPlan> userPlans = planService.loadUserPlans(openId);
        RedirectRouteDto route = new RedirectRouteDto();
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("挑战训练")
                .action("PC点击问题菜单")
                .memo(problemId+"");
        operationLogService.log(operationLog);
        if (userPlans.isEmpty()) {
            // 用户没有报名
            return WebUtils.error("您还没有制定任何计划哦,请先去公众号制定计划吧");
        } else {
            // 已经制定过计划了
            // 查看该问题是否是用户的计划里的
            List<ImprovementPlan> problemMatches = userPlans.stream().filter(item -> item.getProblemId().equals(problemId)).collect(Collectors.toList());
            if (problemMatches.isEmpty()) {
                // 该用户没有制定该问题
                route.setPathName("/servercode");
                return WebUtils.result(route);
            } else {
                // 该用户已经制定该问题的计划
                // 看是否是正在做的
                ImprovementPlan plan = problemMatches.get(0);
                // 获取挑战任务
                List<ChallengePractice> challengePractices = practiceService.getChallengePracticesByProblem(plan.getProblemId());
                if (challengePractices.isEmpty()) {
                    return WebUtils.error("获取挑战任务失败");
                }
                ChallengePractice challenge = challengePractices.get(0);
                if (plan.getStatus() == 1) {
                    // 是正在做的，获取挑战任务
                    ChallengePractice myChallenge = practiceService.getChallengePractice(challenge.getId(), openId, plan.getId());
                    if (myChallenge.getSubmitted()) {
                        // 已提交，进入列表页
                        return WebUtils.result(getFragmentRoute("/fragment/c/list",myChallenge.getId(),null));
                    } else {
                        // 未提交，进入doing页面
                        return WebUtils.result(getFragmentRoute("/fragment/c",challenge.getId(),plan.getId()));
                    }
                } else {
                    // 不是正在做的，获取挑战任务
                    ChallengePractice myChallenge = practiceService.getChallengePracticeNoCreate(challenge.getId(), openId, plan.getId());
                    if (myChallenge.getSubmitted()) {
                        // 已提交，进入列表页
                        return WebUtils.result(getFragmentRoute("/fragment/c/list",myChallenge.getId(),null));
                    } else {
                        // 未提交，进入doing页面,提示无法查看
                        return WebUtils.result(getFragmentRoute("/servercode",null,null));
                    }
                }
            }
        }
    }

    private RedirectRouteDto getFragmentRoute(String pathName,Integer cid,Integer planId){
        RedirectRouteDto route = new RedirectRouteDto();
        route.setPathName(pathName);
        if(cid!=null && planId!=null){
            Map<String,Object> map = Maps.newHashMap();
            if(cid!=null){
                map.put("cid",cid);
            }
            if(planId!=null){
                map.put("planId",planId);
            }
            route.setQuery(map);
        }
        return route;
    }
}
