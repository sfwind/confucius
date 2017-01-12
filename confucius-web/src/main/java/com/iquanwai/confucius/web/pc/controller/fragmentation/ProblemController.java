package com.iquanwai.confucius.web.pc.controller.fragmentation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengePractice;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.resolver.PCLoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.pc.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private ProblemService problemService;
    @Autowired
    private OperationLogService operationLogService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/current")
    public ResponseEntity<Map<String, Object>> loadFragmentPage(PCLoginUser pcLoginUser) {
        try {
            Assert.notNull(pcLoginUser,"用户不能为空");
            // 获取正在做的
            // 获取用户正在进行的任务
            ImprovementPlan runningPlan = planService.getRunningPlan(pcLoginUser.getOpenId());
            // 返回到页面的数据
            // pay：是否付款，status：是否完成，id：难题id,
            ProblemDto dto = new ProblemDto();
            if(runningPlan==null){
                // 没有正在进行的任务（难题），选一个之前做过的
                List<ImprovementPlan> plans = planService.loadUserPlans(pcLoginUser.getOpenId());
                if(plans.isEmpty()){
                    // 没有购买难题,选第一个问题,页面跳转到serverCode
                    List<Problem> problems = problemService.loadProblems();
                    if(problems.isEmpty()){
                        logger.error("problem is empty");
                        return WebUtils.error("获取难题失败");
                    } else {
                        dto.setId(problems.get(0).getId());
                        dto.setPay(false);
                        return WebUtils.result(dto);
                    }
                } else {
                    // 购买过难题，选择最后一个购买的难题,查询挑战任务完成状态
                    ImprovementPlan plan = plans.get(plans.size() - 1);
                    Problem problem =  problemService.getProblem(plan.getProblemId());
                    dto.setPay(true);
                    dto.setId(problem.getId());
                    dto.setStatus(plan.getStatus());
                    List<ChallengePractice> challenges = practiceService.getChallengePracticesByProblem(problem.getId());
                    // 查询挑战任务
                    List<ChallengeDto> challengeSubmits = challenges.stream().map(item ->
                            ChallengeDto.getFromPo(
                                    // 不是正在进行的任务，只查看是否完成
                                    practiceService.getChallengePracticeNoCreate(item.getId(), pcLoginUser.getOpenId(), plan.getId())
                            )
                    ).collect(Collectors.toList());
                    dto.setChallengeList(challengeSubmits);
                    return WebUtils.result(dto);
                }
            } else {
                // 有正在进行的任务(难题)
                Problem problem = problemService.getProblem(runningPlan.getProblemId());
                dto.setPay(true);
                dto.setId(problem.getId());
                dto.setStatus(runningPlan.getStatus());
                // 查看是否提交
                List<ChallengePractice> challenges = practiceService.getChallengePracticesByProblem(problem.getId());
                // 查询挑战任务
                List<ChallengeDto> challengeSubmits = challenges.stream().map(item ->
                        ChallengeDto.getFromPo(
                                // 正在进行的任务可以生成一条记录
                                practiceService.getChallengePractice(item.getId(), pcLoginUser.getOpenId(), runningPlan.getId())
                        )
                ).collect(Collectors.toList());
                dto.setChallengeList(challengeSubmits);
                return WebUtils.result(dto);
            }
        } catch (Exception e) {
            logger.error("pc加载碎片化页面失败", e);
            return WebUtils.error("加载失败");
        }

    }

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
                .memo(problemId + "");
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
                        return WebUtils.result(getFragmentRoute("/fragment/c/list", myChallenge.getId(), null));
                    } else {
                        // 未提交，进入doing页面
                        return WebUtils.result(getFragmentRoute("/fragment/c", challenge.getId(), plan.getId()));
                    }
                } else {
                    // 不是正在做的，获取挑战任务
                    ChallengePractice myChallenge = practiceService.getChallengePracticeNoCreate(challenge.getId(), openId, plan.getId());
                    if (myChallenge.getSubmitted()) {
                        // 已提交，进入列表页
                        return WebUtils.result(getFragmentRoute("/fragment/c/list", myChallenge.getId(), null));
                    } else {
                        // 未提交，进入doing页面,提示无法查看
                        return WebUtils.result(getFragmentRoute("/servercode", null, null));
                    }
                }
            }
        }
    }

    /**
     * 加载问题列表
     */
    @RequestMapping(value = "/list")
    public ResponseEntity<Map<String, Object>> loadProblemList() {
        try {
            List<Problem> problems = problemService.loadProblems();
            List<ProblemListDto> result = Lists.newArrayList();
            problems.forEach(item -> {
                ProblemListDto dto = new ProblemListDto();
                dto.setId(item.getId());
                dto.setProblem(item.getProblem());
            });
            return WebUtils.result(result);
        } catch (Exception e){
            logger.error("加载问题列表失败",e);
            return WebUtils.error("加载问题列表失败");
        }
    }


    private RedirectRouteDto getFragmentRoute(String pathName, Integer cid, Integer planId) {
        RedirectRouteDto route = new RedirectRouteDto();
        route.setPathName(pathName);
        if (cid != null || planId != null) {
            Map<String, Object> map = Maps.newHashMap();
            if (cid != null) {
                map.put("cid", cid);
            }
            if (planId != null) {
                map.put("planId", planId);
            }
            route.setQuery(map);
        }
        return route;
    }
}
