package com.iquanwai.confucius.web.pc.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepoImpl;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.ApplicationService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.exception.ErrorConstants;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationPractice;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.PracticePlan;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.web.pc.dto.HomeworkVoteDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkItemDto;
import com.iquanwai.confucius.web.pc.fragmentation.dto.RiseWorkListDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
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

    public static String challengeListUrl = "/fragment/c/list?cid={cid}";
    public static String doChallengeUrl = "/fragment/c?cid={cid}&planId={planId}";
    public static String serverCode = "/servercode";


    /**
     * 点击fragment菜单后，确定需要跳转到的位置
     */
    @RequestMapping(value = "/community", method = RequestMethod.GET)
    public void fragmentGoWhere(HttpServletRequest request, HttpServletResponse response, PCLoginUser loginUser) {
        /**
         * 1.检查用户是否是我们的学员
         * 2.检查正在进行的任务
         */
        try {
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("训练")
                    .function("碎片化")
                    .action("跳转碎片化页面")
                    .memo("");
            operationLogService.log(operationLog);
            response.sendRedirect("/fragment/rise");
        } catch (Exception e) {
            logger.error("前往碎片化失败");
            return;
        }
    }


    /**
     * 碎片化总任务列表加载
     * @param problemId 问题id
     * @param pcLoginUser 登陆人
     */
    @RequestMapping("/pc/fragment/homework/{problemId}")
    public ResponseEntity<Map<String, Object>> getProblemHomeworkList(@PathVariable Integer problemId, PCLoginUser pcLoginUser) {
        Assert.notNull(problemId, "问题id不能为空");
        Assert.notNull(pcLoginUser, "用户信息能不能为空");
        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("总任务列表加载")
                .memo(problemId+"");
        operationLogService.log(operationLog);
        // 查询该用户有没有购买过这个问题的计划
        List<ImprovementPlan> plans = planService.loadUserPlans(pcLoginUser.getOpenId());
        List<ImprovementPlan> matchPlans = plans.stream().filter(item -> item.getProblemId().equals(problemId)).collect(Collectors.toList());
        RiseWorkListDto riseHomework = new RiseWorkListDto();
        if (matchPlans.isEmpty()) {
            logger.error("用户:{} 未购买专题:{}", pcLoginUser.getOpenId(), problemId);
            return WebUtils.error(ErrorConstants.NOT_PAY_FRAGMENT, "没找到进行中的RISE训练");
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
                        dto.setTitle("设定目标、记录进展、总结心得");
                        dto.setScore(ConfigUtils.getChallengeScore());
                        challengeList.add(dto);
                    }
                });
            });
            riseHomework.setApplicationWorkList(applicationList);
            riseHomework.setChallengeWorkList(challengeList);
            return WebUtils.result(riseHomework);
        }
    }

    /**
     * 点赞或者取消点赞
     *
     * @param vote 1：点赞，2：取消点赞
     */
    @RequestMapping(value = "/pc/fragment/vote", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> vote(PCLoginUser loginUser, @RequestBody HomeworkVoteDto vote) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.isTrue(vote.getStatus() == 1 || vote.getStatus() == 2, "点赞状态异常");
        Integer refer = vote.getReferencedId();
        Integer status = vote.getStatus();
        String openId = loginUser.getOpenId();
        Pair<Integer, String> voteResult;
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("碎片化")
                .function("挑战任务");
        if (status == 1) {
            // 点赞
            practiceService.vote(vote.getType(), refer, openId);
            voteResult = new MutablePair(1, "success");
            operationLog.action("点赞").memo(loginUser.getOpenId() + "点赞" + refer);
        } else {
            // 禁止取消点赞
            logger.error("取消点赞！已禁止!");
            // 取消点赞
//            voteResult = practiceService.disVote(vote.getType(), refer, openId);
//            operationLog.action("取消点赞").memo(loginUser.getOpenId() + "取消点赞" + refer);
        }

        operationLogService.log(operationLog);
        return WebUtils.success();
//        if (voteResult.getLeft() == 1) {
//            return WebUtils.success();
//        } else {
//            return WebUtils.error(voteResult.getRight());
//        }
    }


}
