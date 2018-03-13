package com.iquanwai.confucius.web.pc.backend.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.domain.backend.OperationManagementService;
import com.iquanwai.confucius.biz.domain.backend.ProblemService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.DiscussService;
import com.iquanwai.confucius.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.util.EmployeeService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupChoice;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPractice;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPracticeDiscuss;
import com.iquanwai.confucius.biz.po.quanwai.QuanwaiEmployee;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.web.pc.backend.dto.WarmUpPracticeDto;
import com.iquanwai.confucius.web.resolver.UnionUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by justin on 2017/9/15.
 */
@RestController
@RequestMapping("/pc/operation/warmup")
public class WarmupImportController {
    @Autowired
    private OperationManagementService operationManagementService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private DiscussService discussService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private RedisUtil redisUtil;

    private static final long EXPIRED_TIME = 24 * 60 * 60 * 7;
    private static final String cache_all = "_discuss_all";
    private static final String cache_ignore = "_discuss_ignore";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/list/{problemId}")
    public ResponseEntity<Map<String, Object>> getProblemWarmupPractice(UnionUser loginUser,
                                                                        @PathVariable Integer problemId) {
        List<WarmUpPracticeDto> warmUpPracticeDtos = Lists.newArrayList();
        List<WarmupPractice> warmupPractices = operationManagementService.getPracticeByProblemId(problemId);
        List<ProblemSchedule> problemSchedules = problemService.loadProblemSchedules(problemId);
        warmupPractices.stream().filter(warmupPractice -> warmupPractice.getExample() == false).forEach(warmupPractice -> {
            WarmUpPracticeDto warmUpPracticeDto = new WarmUpPracticeDto();
            BeanUtils.copyProperties(warmupPractice, warmUpPracticeDto);
            ProblemSchedule schedule = problemSchedules.stream().filter(problemSchedule -> problemSchedule.getKnowledgeId().equals(warmupPractice.getKnowledgeId()) && problemSchedule.getDel() == 0).findAny().orElse(null);
            if (schedule != null) {
                warmUpPracticeDto.setChapter(schedule.getChapter());
                warmUpPracticeDto.setSection(schedule.getSection());
                warmUpPracticeDtos.add(warmUpPracticeDto);
            }
        });
        //排序
        List<WarmUpPracticeDto> result = warmUpPracticeDtos.stream().sorted(
                Comparator.comparing(WarmUpPracticeDto::getChapter).
                        thenComparing(Comparator.comparing(WarmUpPracticeDto::getSection).
                                thenComparing(Comparator.comparing(WarmUpPracticeDto::getSequence))))
                .collect(Collectors.toList());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习编辑")
                .action("加载课程的巩固练习");
        operationLogService.log(operationLog);

        return WebUtils.result(result);
    }


    @RequestMapping("/load/all/{problemId}")
    public ResponseEntity<Map<String, Object>> getAllWarmupPractice(UnionUser loginUser,
                                                                    @PathVariable Integer problemId) {
        List<WarmUpPracticeDto> warmUpPracticeDtos = Lists.newArrayList();
        List<WarmupPractice> warmupPractices = operationManagementService.getPracticeByProblemId(problemId);
        List<ProblemSchedule> problemSchedules = problemService.loadProblemSchedules(problemId);
        warmupPractices.forEach(warmupPractice -> {
            WarmUpPracticeDto warmUpPracticeDto = new WarmUpPracticeDto();
            BeanUtils.copyProperties(warmupPractice, warmUpPracticeDto);
            ProblemSchedule schedule = problemSchedules.stream().filter(problemSchedule -> problemSchedule.getKnowledgeId().equals(warmupPractice.getKnowledgeId()) && problemSchedule.getDel() == 0).findAny().orElse(null);
            if (schedule != null) {
                warmUpPracticeDto.setChapter(schedule.getChapter());
                warmUpPracticeDto.setSection(schedule.getSection());
                warmUpPracticeDtos.add(warmUpPracticeDto);
            }
        });
        //排序
        List<WarmUpPracticeDto> result = warmUpPracticeDtos.stream().sorted(
                Comparator.comparing(WarmUpPracticeDto::getExample).reversed().
                        thenComparing(Comparator.comparing(WarmUpPracticeDto::getChapter)).
                        thenComparing(Comparator.comparing(WarmUpPracticeDto::getSection).
                                thenComparing(Comparator.comparing(WarmUpPracticeDto::getSequence))))
                .collect(Collectors.toList());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("巩固练习编辑")
                .action("加载课程的巩固练习");
        operationLogService.log(operationLog);

        return WebUtils.result(result);
    }


    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> savePractice(UnionUser loginUser,
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

    @RequestMapping("/next/{problemId}/{practiceId}")
    public ResponseEntity<Map<String, Object>> getNextPractice(UnionUser loginUser,
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

    @RequestMapping(value = "/insert/practice", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> insertWarmupPractice(UnionUser loginUser, @RequestBody WarmupPractice warmupPractice) {
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
        Integer practiceCnt = practiceService.loadWarmupPracticeCntByPracticeUid(warmupPractice.getPracticeUid());
        if (practiceCnt > 0) {
            return WebUtils.error("当前 UID 选择题已存在，请联系管理员重试");
        }
        Integer knowledgeId = practiceService.insertWarmupPractice(warmupPractice);
        if (knowledgeId <= 0) {
            return WebUtils.error("选择题数据插入失败，请及时练习管理员");
        } else {
            practiceService.insertWarmupChoice(knowledgeId, warmupChoices);
        }
        return WebUtils.success();
    }

    @RequestMapping(value = "/load/problem/{practiceUid}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadProblemByPracticeUid(UnionUser loginUser, @PathVariable String practiceUid) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("后台管理")
                .function("巩固练习新增")
                .action("获取默认课程信息");
        operationLogService.log(operationLog);
        WarmupPractice warmupPractice = practiceService.loadWarmupPracticeByPracticeUid(practiceUid);
        if (warmupPractice != null) {
            return WebUtils.result(warmupPractice);
        } else {
            return WebUtils.error("未找到对应课程数据");
        }
    }

    @RequestMapping("/delete/example/{id}")
    public ResponseEntity<Map<String, Object>> deleteExample(UnionUser unionUser, @PathVariable Integer id) {
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("后台管理")
                .function("选择题管理")
                .action("删除例题");
        operationLogService.log(operationLog);

        Integer result = practiceService.deleteExamples(id);
        if (result == 1) {
            return WebUtils.success();
        }
        return WebUtils.error("删除失败");

    }


    @RequestMapping(value = "/load/discuss", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadTargetDiscuss(@RequestParam("interval") Integer interval) {
        String currentDate = DateUtils.parseDateToString(DateUtils.beforeDays(new Date(), interval));

        List<WarmupPracticeDiscuss> discusses = (List<WarmupPracticeDiscuss>)redisUtil.get(currentDate+cache_all,List.class);

        if(discusses==null){
            logger.info("读取数据库");
            discusses = discussService.loadCurrentDayDiscuss(currentDate);
            redisUtil.set(currentDate+cache_all,discusses,EXPIRED_TIME);
        }
        //获取过滤后待评论的选择题
        List<WarmupPractice> warmupPractices = getTargetWarmup(discusses, currentDate);

        return WebUtils.result(warmupPractices);
    }

    @RequestMapping(value = "/ignore/discuss", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> ignoreDiscuss(@RequestParam("discussId") Integer id, @RequestParam("interval") Integer interval) {
        String currentDate = DateUtils.parseDateToString(DateUtils.beforeDays(new Date(), interval));

        String ignoreString = redisUtil.get(currentDate+cache_ignore);
        if (ignoreString == null) {
            redisUtil.set(currentDate+cache_ignore, id, new Long(EXPIRED_TIME));
        } else {
            ignoreString = ignoreString+"," + id;
            redisUtil.set(currentDate+cache_ignore, ignoreString, new Long(EXPIRED_TIME));
        }
        return WebUtils.success();
    }

    @RequestMapping(value = "/load/target/{warmupPracticeId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadTargetPractice(@PathVariable Integer warmupPracticeId, @RequestParam("interval") Integer interval) {
        String currentDate = DateUtils.parseDateToString(DateUtils.beforeDays(new Date(), interval));
        List<WarmupPracticeDiscuss> warmupPracticeDiscusses = discussService.loadTargetDiscuss(warmupPracticeId, currentDate);
        warmupPracticeDiscusses = filterDiscuss(warmupPracticeDiscusses, currentDate);
        WarmupPractice warmupPractice = operationManagementService.getTargetPractice(warmupPracticeId, warmupPracticeDiscusses);
        return WebUtils.result(warmupPractice);
    }

    private List<WarmupPractice> getTargetWarmup(List<WarmupPracticeDiscuss> warmupPracticeDiscusses, String currentDate) {
        //过滤评论
        warmupPracticeDiscusses = filterDiscuss(warmupPracticeDiscusses, currentDate);
        //获取过滤后的选择题id
        List<Integer> practiceIds = warmupPracticeDiscusses.stream().map(WarmupPracticeDiscuss::getWarmupPracticeId).distinct().collect(Collectors.toList());
        //查看今天的选择题
        List<WarmupPractice> warmupPractices = practiceService.loadWarmupPractices(practiceIds);

        return warmupPractices;
    }


    /**
     * 过滤不需要展示的评论（员工评论或者被员工评论过的评论或者被忽略的评论）
     *
     * @param warmupPracticeDiscusses
     * @return
     */
    private List<WarmupPracticeDiscuss> filterDiscuss(List<WarmupPracticeDiscuss> warmupPracticeDiscusses, String currentDate) {
        //获得员工
        List<QuanwaiEmployee> quanwaiEmployees = employeeService.getEmployees();

        warmupPracticeDiscusses = warmupPracticeDiscusses.stream().map(warmupPracticeDiscuss -> {
            if (quanwaiEmployees.stream().filter(quanwaiEmployee -> quanwaiEmployee.getProfileId().equals(warmupPracticeDiscuss.getProfileId())).count() == 0) {
                return warmupPracticeDiscuss;
            } else {
                return null;
            }
        }).filter(warmupPracticeDiscuss -> warmupPracticeDiscuss != null).collect(Collectors.toList());

        List<Integer> replyIds = warmupPracticeDiscusses.stream().map(WarmupPracticeDiscuss::getId).collect(Collectors.toList());

        List<WarmupPracticeDiscuss> replayList = discussService.loadByReplys(replyIds);
        List<Integer> emplyeeProfileIds = quanwaiEmployees.stream().map(QuanwaiEmployee::getProfileId).distinct().collect(Collectors.toList());
        //过滤被忽略的discuss
        String ignoreStrings = redisUtil.get(currentDate+cache_ignore);
        if (ignoreStrings != null) {
            String[] ignoreDiscusses = ignoreStrings.split(",");
            warmupPracticeDiscusses = warmupPracticeDiscusses.stream().map(warmupPracticeDiscuss -> {
                for (String ignore : ignoreDiscusses) {
                    if (warmupPracticeDiscuss.getId() == Integer.valueOf(ignore)) {
                        return null;
                    }
                }
                return warmupPracticeDiscuss;
            }).filter(warmupPracticeDiscuss -> warmupPracticeDiscuss != null).collect(Collectors.toList());
        }
        //过滤被员工评论过的评论
        return warmupPracticeDiscusses.stream().map(warmupPracticeDiscuss -> {
            for (WarmupPracticeDiscuss reply : replayList) {
                if (warmupPracticeDiscuss.getId() == reply.getRepliedId() && emplyeeProfileIds.contains(reply.getProfileId())) {
                    return null;
                }
            }
            return warmupPracticeDiscuss;
        }).filter(warmupPracticeDiscuss -> warmupPracticeDiscuss != null).collect(Collectors.toList());
    }

}
