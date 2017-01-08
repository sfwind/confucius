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
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.resolver.PCLoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.account.dto.FragmentDto;
import com.iquanwai.confucius.web.pc.dto.FragmentPageDto;
import com.iquanwai.confucius.web.pc.dto.ProblemDto;
import com.iquanwai.confucius.web.pc.dto.RedirectRouteDto;
import okhttp3.Route;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
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
@RequestMapping
public class FragmentController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PlanService planService;
    @Autowired
    private PracticeService practiceService;
    public static String challengeListUrl = "/fragment/c/list?cid={cid}";
    public static String doChallengeUrl = "/fragment/c?cid={cid}&planId={planId}";
    public static String serverCode = "/servercode";




    /**
     * 点击fragment菜单后，确定需要跳转到的位置
     */
    @RequestMapping(value = "/community", method = RequestMethod.GET)
    public void fragmentGoWhere(HttpServletRequest request, HttpServletResponse response, PCLoginUser pcLoginUser) {
        /**
         * 1.检查用户是否是我们的学员
         * 2.检查正在进行的任务
         */
        try {
            String openId = pcLoginUser.getOpenId();
            List<ImprovementPlan> plans = planService.loadUserPlans(openId);
            if (plans.isEmpty()) {
                // 不是碎片化的学员，进入扫公众号关注的界面
                response.sendRedirect(serverCode);
                return;
            } else {
                OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                        .module("课程")
                        .function("碎片化")
                        .action("进入")
                        .memo("");
                operationLogService.log(operationLog);
                // 查询正在进行的计划
                ImprovementPlan runningPlan = planService.getRunningPlan(openId);
                if (runningPlan != null) {
                    Pair<Integer, String> pair = getRunningPlanRoute(runningPlan, openId);
                    if (pair.getLeft() == 200) {
                         WebUtils.redirect(request,response,pair.getRight());
                         return;
                    } else {
                        // 生成失败
                        WebUtils.redirectError(request,response,"生成挑战任务失败 ");
                        return;
                    }
                } else {
                    // 没有正在进行的任务,找到一个用户提交过的
                    for (ImprovementPlan plan : plans) {
                        // 获取所有challenge
                        List<ChallengePractice> cList = practiceService.getChallengePracticesByProblem(plan.getProblemId());
                        List<ChallengePractice> sList = cList.stream()
                                .map(item -> practiceService.getChallengePracticeNoCreate(item.getId(), openId, plan.getId()))
                                .filter(ChallengePractice::getSubmitted)
                                .collect(Collectors.toList());
                        if (!sList.isEmpty()) {
                            // 找到了,做完的
                            ChallengePractice mySubmitted = sList.get(0);
                            Map<String, String> params = Maps.newHashMap();
                            params.put("cid", mySubmitted.getId()+"");
                            WebUtils.redirect(request,response,CommonUtils.placeholderReplace(challengeListUrl,params));
                            return;
                        }
                    }
                    // 没有正在进行的 也没有做完的。。。。。
                    WebUtils.redirectError(request,response,"没有能够查看的作业OTZ");
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("前往碎片化失败");
            return;
        }
    }

    /**
     * 获取用户在这个计划里应该去的路径
     */
    private Pair<Integer, String> getRunningPlanRoute(ImprovementPlan plan, String openId) {
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
            if (wannaChallenge.getSubmitted()) {
                // 已提交，进入list
                Map<String, String> params = Maps.newHashMap();
                params.put("cid", wannaChallenge.getId()+"");
                params.put("debug","true");
                return new MutablePair<Integer, String>(200, CommonUtils.placeholderReplace(challengeListUrl,params));
            } else {
                // 未提交，进入doing
                Map<String, String> params = Maps.newHashMap();
                params.put("cid", wannaChallenge.getId()+"");
                params.put("planId", plan.getId()+"");
                params.put("debug","true");

                return new MutablePair<Integer, String>(200, CommonUtils.placeholderReplace(doChallengeUrl,params));
            }
        } else {
            // 该计划找不到挑战任务，报错
            return new MutablePair<Integer, String>(221, null);
        }
    }


}
