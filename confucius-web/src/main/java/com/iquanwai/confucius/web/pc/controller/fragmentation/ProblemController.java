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
import com.iquanwai.confucius.web.pc.dto.FragmentPageDto;
import com.iquanwai.confucius.web.pc.dto.ProblemDto;
import com.iquanwai.confucius.web.pc.dto.RedirectRouteDto;
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

    @RequestMapping("/problems")
    public ResponseEntity<Map<String, Object>> loadFragmentPage(PCLoginUser pcLoginUser) {
        try {
//            Assert.notNull(pcLoginUser, "用户不能为空");
            /**
             * 1.加载所有挑战作业
             * 2.查出用户正在进行的训练计划
             * 3.根据训练计划id查询挑战任务
             */
            OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                    .module("PC")
                    .function("碎片化")
                    .action("进入碎片化页面")
                    .memo(null);
            operationLogService.log(operationLog);
            // 加载所有问题
            List<Problem> problems = problemService.loadProblems();
            List<ProblemDto> problemDtos = Lists.newArrayList();
            // 初始化问题list
            problemDtos.addAll(problems.stream().map(problem -> {
                ProblemDto problemDto = new ProblemDto();
                problemDto.setId(problem.getId());
                problemDto.setPic(problem.getPic());
                problemDto.setProblem(problem.getProblem());
                return problemDto;
            }).collect(Collectors.toList()));
            // 获取用户已经付过钱的计划
            List<ImprovementPlan> improvementPlans = planService.loadUserPlans(pcLoginUser.getOpenId());
            Map<Integer, Integer> paiedPlan = Maps.newHashMap();
            // 记录问题对应的status
            improvementPlans.forEach(item -> {
                paiedPlan.put(item.getProblemId(), item.getStatus());
            });
            // 设置问题付费状态
            problemDtos.forEach(item -> {
                item.setPay(paiedPlan.containsKey(item.getId()));
                item.setStatus(paiedPlan.get(item.getId()));
            });
            Integer doingId = null;
            for (Map.Entry<Integer, Integer> pair : paiedPlan.entrySet()) {
                if (pair.getValue() == 1) {
                    doingId = pair.getKey();
                }
            }
            // 如果没有正在进行的，就取第一个
            doingId = doingId == null ? problemDtos.get(0).getId() : doingId;
            FragmentPageDto fragmentPageDto = new FragmentPageDto();
            fragmentPageDto.setProblemList(problemDtos);
            fragmentPageDto.setDoingId(doingId);
            return WebUtils.result(fragmentPageDto);
        } catch (Exception e) {
            logger.error("pc加载碎片化页面失败", e.getLocalizedMessage());
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
        if(cid!=null || planId!=null){
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
