package com.iquanwai.confucius.web.pc.fragmentation.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.Practice;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepoImpl;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.ApplicationService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.ChallengeService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.resolver.PCLoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.account.dto.FragmentDto;
import com.iquanwai.confucius.web.pc.dto.*;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkItemDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkListDto;
import okhttp3.Route;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private ChallengeService challengeService;

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
            response.sendRedirect("/fragment/rise");
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
                params.put("cid", wannaChallenge.getId() + "");
                params.put("debug", "true");
                return new MutablePair<Integer, String>(200, CommonUtils.placeholderReplace(challengeListUrl, params));
            } else {
                // 未提交，进入doing
                Map<String, String> params = Maps.newHashMap();
                params.put("cid", wannaChallenge.getId() + "");
                params.put("planId", plan.getId() + "");
                params.put("debug", "true");

                return new MutablePair<Integer, String>(200, CommonUtils.placeholderReplace(doChallengeUrl, params));
            }
        } else {
            // 该计划找不到挑战任务，报错
            return new MutablePair<Integer, String>(221, null);
        }
    }


    @RequestMapping("/pc/fragment/homework/{problemId}")
    public ResponseEntity<Map<String, Object>> getProblemHomeworkList(@PathVariable Integer problemId, PCLoginUser pcLoginUser) {
        try {
            Assert.notNull(problemId, "问题id不能为空");
            Assert.notNull(pcLoginUser, "用户信息能不能为空");
            // 查询该用户有没有购买过这个问题的计划
            List<ImprovementPlan> plans = planService.loadUserPlans(pcLoginUser.getOpenId());
            List<ImprovementPlan> matchPlans = plans.stream().filter(item -> item.getProblemId().equals(problemId)).collect(Collectors.toList());
            RiseWorkListDto riseHomework = new RiseWorkListDto();
            if (matchPlans.isEmpty()) {
                logger.error("用户:{} 未购买主题:{}", pcLoginUser.getOpenId(), problemId);
                return WebUtils.error(ErrorConstants.NOT_PAY_FRAGMENT, "您还未购买过该碎片化主题");
            } else {
                // 购买过专题
                List<RiseWorkItemDto> challengeList = Lists.newArrayList();
                List<RiseWorkItemDto> applicationList = Lists.newArrayList();
                matchPlans.forEach(plan -> {
                    // 查询该plan的任务列表
                    List<PracticePlan> practicePlans = planService.loadWorkPlanList(plan.getId());
                    practicePlans.forEach(item -> {
                        RiseWorkItemDto dto = new RiseWorkItemDto();
                        dto.setPlanId(plan.getId());
                        dto.setType(item.getType());
                        dto.setUnlocked(item.getUnlocked());
                        dto.setWorkId(Integer.parseInt(item.getPracticeId()));
                        dto.setStatus(item.getStatus());
                        if (item.getType() == Constants.PracticeType.APPLICATION) {
                            ApplicationPractice applicationPractice = applicationService.loadApplicationPractice(Integer.parseInt(item.getPracticeId()));
                            if (applicationPractice == null) {
                                logger.error("查询应用训练失败,训练计划:{}", item);
                            } else {
                                dto.setTitle(applicationPractice.getTopic());
                                dto.setScore(PointRepoImpl.score.get(applicationPractice.getDifficulty()));
                                applicationList.add(dto);
                            }
                        } else {
                            dto.setTitle("挑战任务");
                            // TODO 分数 可配置
                            dto.setScore(500);
                            challengeList.add(dto);
                        }
                    });
                });
                riseHomework.setApplicationWorkList(applicationList);
                riseHomework.setChallengeWorkList(challengeList);
                return WebUtils.result(riseHomework);
            }
        } catch (Exception e) {
            logger.error("加载作业list失败", e);
            return WebUtils.error(e.getLocalizedMessage());
        }
    }

    /**
     * 点赞或者取消点赞
     *
     * @param vote 1：点赞，2：取消点赞
     */
    @RequestMapping(value = "/pc/fragment/vote", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> vote(PCLoginUser loginUser, @RequestBody HomeworkVoteDto vote) {
        try {
            Assert.notNull(loginUser, "用户不能为空");
            Assert.isTrue(vote.getStatus() == 1 || vote.getStatus() == 2, "点赞状态异常");
            Integer refer = vote.getReferencedId();
            Integer status = vote.getStatus();
            String openId = loginUser.getOpenId();
            Pair<Integer, String> voteResult = null;
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("碎片化")
                    .function("挑战任务");
            if (status == 1) {
                // 点赞
                practiceService.vote(vote.getType(), refer, openId);
                voteResult = new MutablePair<Integer, String>(1, "success");
                operationLog.action("点赞").memo(loginUser.getOpenId() + "点赞" + refer);
            } else {
                // 取消点赞
                voteResult = practiceService.disVote(vote.getType(), refer, openId);
                operationLog.action("取消点赞").memo(loginUser.getOpenId() + "取消点赞" + refer);
            }

            operationLogService.log(operationLog);
            if (voteResult.getLeft() == 1) {
                return WebUtils.success();
            } else {
                return WebUtils.error(voteResult.getRight());
            }
        } catch (Exception e) {
            logger.error("点赞失败,{}", e.getLocalizedMessage());
            return WebUtils.error("点赞失败");
        }
    }


}
