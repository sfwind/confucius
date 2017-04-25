package com.iquanwai.confucius.web.course.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.customer.ProfileService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.po.systematism.Course;
import com.iquanwai.confucius.biz.po.systematism.CourseWeek;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.util.ErrorMessageUtils;
import com.iquanwai.confucius.web.course.dto.CertificateDto;
import com.iquanwai.confucius.web.course.dto.CoursePageDto;
import com.iquanwai.confucius.web.course.dto.WeekIndexDto;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/8/25.
 */
@RestController
@RequestMapping("/course")
public class CourseController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private CourseProgressService courseProgressService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ProfileService profileService;

    private String[] WEEK_INDEXES = {"开营前", "第一周", "第二周", "第三周"};

    @RequestMapping("/load/{courseId}")
    public ResponseEntity<Map<String, Object>> loadCourse(LoginUser loginUser, @PathVariable Integer courseId){
        Assert.notNull(loginUser,"用户不能为空");
        Course c = courseProgressService.loadCourse(courseId);

        ClassMember classMember = courseProgressService.loadActiveCourse(loginUser.getOpenId(), courseId);
        if(classMember==null){
            LOGGER.error("用户"+loginUser.getWeixinName()+"还没有报名, openid is {}", loginUser.getOpenId());
            return WebUtils.error(ErrorMessageUtils.getErrmsg("course.load.nopaid"));
        }
        int week = getProgressWeek(classMember, c.getType());
        CoursePageDto course = getCourse(loginUser, classMember, week, c);
        if(course==null){
            return WebUtils.error("获取当前课程失败");
        }
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("课程")
                .function("学习课程")
                .action("打开课程引导页")
                .memo(course.getCourse().getId()+"");
        operationLogService.log(operationLog);
        return WebUtils.result(course);
    }

    private int getProgressWeek(ClassMember classMember, int type) {
        //短课程,试听课默认返回第一周
        if(type==Course.SHORT_COURSE || type==Course.AUDITION_COURSE){
            return 1;
        }
        String personalProgress = classMember.getProgress();
        int last = -99;
        if(StringUtils.isNotEmpty(personalProgress)) {
            String[] progressArr = personalProgress.split(",");
            for (String aProgressArr : progressArr) {
                try {
                    int that = Integer.valueOf(aProgressArr);
                    if (that > last) {
                        last = that;
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error(classMember.getOpenId() + " progress is abnormal," + aProgressArr + " is not a number");
                }
            }
        }

        if(last<=0){
            return 0;
        }else {
            return (last - 1) / 7 + 1;
        }
    }

    private CoursePageDto getCourse(LoginUser loginUser, ClassMember classMember,
                                    int week, Course course) {

        courseProgressService.loadChapter(classMember, week, course);
        //设置看到某一页
        courseProgressService.personalChapterPage(loginUser.getOpenId(), course.getChapterList());
        CoursePageDto coursePageDto = new CoursePageDto();
        coursePageDto.setCourse(course);
        //加载周主题
        CourseWeek courseWeek = courseProgressService.loadCourseWeek(classMember.getCourseId(), week);
        coursePageDto.setWeek(week);
        if(courseWeek!=null) {
            coursePageDto.setTopic(courseWeek.getTopic());
        }
        //加载每周的index显示
        setWeekIndex(coursePageDto, course);
        return coursePageDto;
    }

    private void setWeekIndex(CoursePageDto coursePageDto, Course course) {
        List<WeekIndexDto> weekIndexes = Lists.newArrayList();
        if(course.isPreChapter()){
            WeekIndexDto weekIndexDto = new WeekIndexDto();
            weekIndexDto.setIndex(0);
            weekIndexDto.setIndexName(WEEK_INDEXES[0]);
            weekIndexes.add(weekIndexDto);
        }
        for(int i=1;i<=course.getWeek();i++){
            WeekIndexDto weekIndexDto = new WeekIndexDto();
            weekIndexDto.setIndex(i);
            weekIndexDto.setIndexName(WEEK_INDEXES[i]);
            weekIndexes.add(weekIndexDto);
        }
        coursePageDto.setWeekIndex(weekIndexes);
    }

    @RequestMapping("/week/{courseId}/{week}")
    public ResponseEntity<Map<String, Object>> loadWeek(@PathVariable("courseId") Integer courseId,
                                                        @PathVariable("week") Integer week,
                                                        LoginUser loginUser){
        Assert.notNull(loginUser,"用户不能为空");
        ClassMember classMember = courseProgressService.loadActiveCourse(loginUser.getOpenId(), courseId);
        if(classMember==null){
            WebUtils.error("用户"+loginUser.getWeixinName()+"还没有报名");
        }
        Course c = courseProgressService.loadCourse(courseId);

        CoursePageDto course = getCourse(loginUser, classMember, week, c);
        if(course==null){
            return WebUtils.error("获取用户当前课程失败");
        }
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("课程")
                .function("学习课程")
                .action("打开某一周的课程")
                .memo(courseId+","+week);
        operationLogService.log(operationLog);
        return WebUtils.result(course);
    }

    @RequestMapping("/certificate/info/{courseId}")
    public ResponseEntity<Map<String, Object>> certificateInfo(@PathVariable("courseId") Integer courseId,
                                                               LoginUser loginUser){
        Assert.notNull(loginUser,"用户不能为空");
        CertificateDto certificateDto = new CertificateDto();
        Profile account = profileService.getProfile(loginUser.getOpenId());
        if(account!=null){
            certificateDto.setName(account.getRealName());
        }
        // TODO:传classId
        List<ClassMember> classMemberList = courseProgressService.loadGraduateClassMember(loginUser.getOpenId(), courseId);
        if(CollectionUtils.isNotEmpty(classMemberList)){
            ClassMember classMember = classMemberList.get(0);
            certificateDto.setCertificateNo(classMember.getCertificateNo());
            Course course = courseProgressService.loadCourse(classMember.getCourseId());
            if(course!=null){
                certificateDto.setCertificateBg(course.getCertificatePic());
            }
            certificateDto.setComment(courseProgressService.certificateComment(course.getName(), classMember));
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("课程")
                .function("毕业")
                .action("打开证书")
                .memo(courseId+"");
        operationLogService.log(operationLog);
        return WebUtils.result(certificateDto);
    }
}
