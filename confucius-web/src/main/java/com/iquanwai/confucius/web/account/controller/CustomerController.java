package com.iquanwai.confucius.web.account.controller;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.customer.ProfileService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.Region;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.ImprovementPlan;
import com.iquanwai.confucius.biz.po.fragmentation.Knowledge;
import com.iquanwai.confucius.biz.po.fragmentation.Problem;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.po.systematism.Course;
import com.iquanwai.confucius.web.account.dto.AreaDto;
import com.iquanwai.confucius.web.account.dto.CourseDto;
import com.iquanwai.confucius.web.account.dto.PlanDto;
import com.iquanwai.confucius.web.account.dto.PlanListDto;
import com.iquanwai.confucius.web.account.dto.ProfileDto;
import com.iquanwai.confucius.web.account.dto.RegionDto;
import com.iquanwai.confucius.web.account.dto.RiseDto;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2017/2/4.
 */
@RestController
@RequestMapping("/customer")
public class CustomerController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountService accountService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PlanService planService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private CourseProgressService courseProgressService;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private SignupService signupService;

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadProfile(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("个人信息")
                .action("加载个人信息");
        operationLogService.log(operationLog);
        ProfileDto profileDto = new ProfileDto();
        Profile account = profileService.getProfile(loginUser.getOpenId());

        try {
            BeanUtils.copyProperties(profileDto,account);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("beanUtils copy props error",e);
            return WebUtils.error("加载个人信息失败");
        }
        // 查询id
        Region city = accountService.loadCityByName(account.getCity());
        Region province = accountService.loadProvinceByName(account.getProvince());
        profileDto.setCityId(city == null ? null : city.getId());
        profileDto.setProvinceId(province == null ? null : province.getId());
        return WebUtils.result(profileDto);
    }

    @RequestMapping(value = "/profile", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitProfile(LoginUser loginUser, @RequestBody ProfileDto profileDto) {
        Assert.notNull(loginUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("个人信息")
                .action("提交个人信息");
        operationLogService.log(operationLog);
        Profile profile =  new Profile();
        try {
            BeanUtils.copyProperties(profile,profileDto);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("beanUtils copy props error",e);
            return WebUtils.error("提交个人信息失败");
        }
        profile.setOpenid(loginUser.getOpenId());
        profileService.submitPersonalCenterProfile(profile);
        return WebUtils.success();
    }


    @RequestMapping("/region")
    public ResponseEntity<Map<String, Object>> loadRegion() {
        List<Region> provinces = accountService.loadAllProvinces();
        List<Region> cities = accountService.loadCities();
        RegionDto regionDto = new RegionDto();
        regionDto.setProvinceList(provinces.stream().map(item -> new AreaDto(item.getId() + "", item.getName(), item.getParentId() + "")).collect(Collectors.toList()));
        regionDto.setCityList(cities.stream().map(item -> new AreaDto(item.getId() + "", item.getName(), item.getParentId() + "")).collect(Collectors.toList()));
        return WebUtils.result(regionDto);
    }

    @RequestMapping(value = "/rise", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadRiseInfo(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("RISE")
                .action("查询rise信息");
        operationLogService.log(operationLog);
        Profile profile = profileService.getProfile(loginUser.getOpenId());
        RiseDto riseDto = new RiseDto();
        riseDto.setPoint(profile.getPoint());
        return WebUtils.result(riseDto);
    }

    @RequestMapping(value = "/rise/plans", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadUserPlans(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("RISE")
                .action("查询专题信息");
        operationLogService.log(operationLog);
        List<ImprovementPlan> plans = planService.loadUserPlans(loginUser.getOpenId());
        PlanListDto list = new PlanListDto();
        List<PlanDto> runningPlans = Lists.newArrayList();
        List<PlanDto> donePlans = Lists.newArrayList();
        plans.forEach(item -> {
            PlanDto planDto = new PlanDto();
            planDto.setName(problemService.getProblem(item.getProblemId()).getProblem());
            planDto.setPoint(item.getPoint());
            planDto.setProblemId(item.getProblemId());
            planDto.setPlanId(item.getId());
            if (item.getStatus() == 1 || item.getStatus() == 2) {
                runningPlans.add(planDto);
            } else if (item.getStatus() == 3) {
                donePlans.add(planDto);
            }
        });
        list.setRunningPlans(runningPlans);
        list.setDonePlans(donePlans);
        // 查询riseId
        Profile profile = accountService.getProfile(loginUser.getOpenId(), false);
        list.setRiseId(profile.getRiseId());
        list.setRiseMember(profile.getRiseMember());
        if (profile.getRiseMember()) {

        }
        return WebUtils.result(list);
    }


    @RequestMapping(value = "/course/list", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadUserCourses(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("训练营")
                .action("查询报过的课程列表 ");
        operationLogService.log(operationLog);
        // 查询是否有真实姓名
        Profile profile = profileService.getProfile(loginUser.getOpenId());
        List<ClassMember> classMembers = courseProgressService.loadClassMembers(loginUser.getOpenId());
        List<CourseDto> list = classMembers
                .stream().map(item -> {
                    CourseDto dto = new CourseDto();
                    Course course = courseProgressService.loadCourse(item.getCourseId());
                    if(course.getType()==Course.AUDITION_COURSE){
                        return null;
                    } else {
                        dto.setName(course.getName());
                        dto.setId(course.getId());
                        dto.setHasCertificateNo(item.getCertificateNo() != null);
                        dto.setHasRealName(profile.getRealName() != null);
                        dto.setNoCertificate(course.getType()==Course.SHORT_COURSE);
                        dto.setGraduate(item.getGraduate());
                        return dto;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
        return WebUtils.result(list);
    }

    @RequestMapping(value = "/riseid", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadRiseId(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("用户信息")
                .action("查询RiseId ");
        operationLogService.log(operationLog);
        Profile profile = profileService.getProfile(loginUser.getOpenId());
        if(profile==null){
            logger.error("用户:{} 缺少Profile信息，进入个人中心失败",loginUser.getOpenId());
            return WebUtils.error("数据异常，请联系管理员");
        }
        return WebUtils.result(profile.getRiseId());
    }

    @RequestMapping("/rise/knowledge/{problemId}")
    public ResponseEntity<Map<String,Object>> loadKnowledgeList(LoginUser loginUser,@PathVariable Integer problemId){
        List<Knowledge> problemKnowledgeList = planService.getProblemKnowledgeList(problemId);
        // 查看该用户是否对该问题评分
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("RISE")
                .action("打开专题信息")
                .module(problemId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(problemKnowledgeList);
    }


    @RequestMapping("/rise/get/{problemId}")
    public ResponseEntity<Map<String, Object>> loadProblem(LoginUser loginUser, @PathVariable Integer problemId){
        Assert.notNull(loginUser, "用户不能为空");
        Problem problem = problemService.getProblem(problemId);
        // 查看该用户是否对该问题评分
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("RISE")
                .action("打开专题详情页")
                .module(problemId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(problem);
    }

    @RequestMapping("/rise/knowledge/load/{knowledgeId}")
    public ResponseEntity<Map<String, Object>> loadKnowledge(LoginUser loginUser,
                                                             @PathVariable Integer knowledgeId){
        Assert.notNull(loginUser, "用户不能为空");
//        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
//        if(improvementPlan==null){
//            logger.error("{} has no improvement plan", loginUser.getOpenId());
//            return WebUtils.result("您还没有制定训练计划哦");
//        }
        Knowledge knowledge = planService.getKnowledge(knowledgeId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("RISE")
                .action("打开知识点回顾页")
                .memo(knowledgeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(knowledge);
    }

    @RequestMapping("/rise/feedback/open")
    public ResponseEntity<Map<String,Object>> openFeedBack(LoginUser loginUser){
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("帮助")
                .action("打开帮助页面")
                .memo("");
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping("/rise/member")
    public ResponseEntity<Map<String,Object>> riseMember(LoginUser loginUser){
        RiseMember riseMember = signupService.currentRiseMember(loginUser.getOpenId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("用户信息")
                .function("RISE")
                .action("查询rise会员信息")
                .memo(riseMember!=null?new Gson().toJson(riseMember):"none");
        operationLogService.log(operationLog);
        return WebUtils.result(riseMember);
    }
}
