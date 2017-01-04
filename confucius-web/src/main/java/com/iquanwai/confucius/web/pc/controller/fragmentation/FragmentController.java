package com.iquanwai.confucius.web.pc.controller.fragmentation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.Practice;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengePractice;
import com.iquanwai.confucius.biz.po.fragmentation.ChallengeSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.resolver.PCLoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.account.dto.FragmentDto;
import com.iquanwai.confucius.web.pc.dto.FragmentPageDto;
import com.iquanwai.confucius.web.pc.dto.ProblemDto;
import com.iquanwai.confucius.web.pc.dto.RedirectRouteDto;
import okhttp3.Route;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2016/12/29.
 */
@RestController
@RequestMapping("/pc/fragment")
public class FragmentController {
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private PlanService planService;
    @Autowired
    private PracticeService practiceService;

    @RequestMapping("/page")
    public ResponseEntity<Map<String, Object>> loadFragmentPage(PCLoginUser pcLoginUser) {
        // 这里检查一遍是否付费
        // 进入fragmentpage
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
    }

    /**
     * 点击fragment菜单后，确定需要跳转到的位置
     */
    @RequestMapping(value = "/where", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> fragmentGoWhere(HttpServletRequest request, HttpServletResponse response, PCLoginUser pcLoginUser) {
        /**
         * 1.检查用户是否是我们的学员
         * 2.检查正在进行的任务
         */
        String openId = pcLoginUser.getOpenId();
        List<ImprovementPlan> plans = planService.loadUserPlans(openId);
        if (plans.isEmpty()) {
            // 不是碎片化的学员，进入扫公众号关注的界面
            return WebUtils.result(new RedirectRouteDto("/servercode", null));
        } else {
            OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                    .module("课程")
                    .function("碎片化")
                    .action("进入")
                    .memo("");
            operationLogService.log(operationLog);
            // 是公众号学员
            ImprovementPlan runningPlan = planService.getRunningPlan(openId);
            if (runningPlan != null) {
                Pair<Integer, RedirectRouteDto> pair = getRunningPlanRoute(runningPlan, openId);
                if (pair.getLeft() == 200) {
                    return WebUtils.result(pair.getRight());
                } else {
                    // 生成失败
                    return WebUtils.error("生成挑战任务失败 ");
                }
            } else {
                // 没有正在进行的任务,找到一个用户提交过的
                for(ImprovementPlan plan:plans){
                    // 获取所有challenge
                    List<ChallengePractice> cList = practiceService.getChallengePracticesByProblem(plan.getProblemId());
                    List<ChallengePractice> sList = cList.stream()
                            .map(item -> practiceService.getChallengePracticeNoCreate(item.getId(), openId, plan.getId()))
                            .filter(ChallengePractice::getSubmitted)
                            .collect(Collectors.toList());
                    if(!sList.isEmpty()){
                        // 找到了,做完的
                        ChallengePractice mySubmitted = sList.get(0);
                        RedirectRouteDto route = new RedirectRouteDto();
                        route.setPathName("/fragment/c/list");
                        Map<String,Object> params = Maps.newHashMap();
                        params.put("cid",mySubmitted.getId());
                        route.setQuery(params);
                        return WebUtils.result(route);
                    }
                }
                // 没有正在进行的 也没有做完的。。。。。
                return WebUtils.error("没有能够查看的作业OTZ");
            }
        }
    }

    public static void main(String[] args){
        Pattern pattern = Pattern.compile("/pc/fragment/.*");
        System.out.print( pattern.matcher("/pc/fragment/test/gg?33ff=f").matches());

    }




    /**
     * 获取用户在这个计划里应该去的路径
     */
    private Pair<Integer, RedirectRouteDto> getRunningPlanRoute(ImprovementPlan plan, String openId) {
        ChallengePractice wannaChallenge = null;
        // 查询挑战任务
        List<ChallengePractice> challengePractices = practiceService.loadPractice(plan.getProblemId());
        List<ChallengePractice> submits = challengePractices.stream().map(item -> {
            // 找到用户提交的记录
            return practiceService.getChallengePractice(item.getId(), openId, plan.getId());
        }).collect(Collectors.toList());
        // 目前挑战任务只有一个
        if (!submits.isEmpty()) {
            // 取出第一个
            wannaChallenge = submits.get(0);
            RedirectRouteDto route = new RedirectRouteDto();
            if (wannaChallenge.getSubmitted()) {
                // 已提交，进入list
                route.setPathName("/fragment/c/list");
                Map<String, Object> params = Maps.newHashMap();
                params.put("cid", wannaChallenge.getId());
//                        params.put("planId",runningPlan.getId());
                route.setQuery(params);
                return new MutablePair<Integer, RedirectRouteDto>(200, route);
            } else {
                // 未提交，进入doing
                route.setPathName("/fragment/c");
                Map<String, Object> params = Maps.newHashMap();
                params.put("cid", wannaChallenge.getId());
                params.put("planId", plan.getId());
                route.setQuery(params);
                return new MutablePair<Integer, RedirectRouteDto>(200, route);
            }
        } else {
            // 该计划找不到挑战任务，报错
            return new MutablePair<Integer, RedirectRouteDto>(221, null);
        }
    }


}
