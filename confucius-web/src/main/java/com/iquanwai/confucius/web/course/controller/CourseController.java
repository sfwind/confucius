package com.iquanwai.confucius.web.course.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.ClassMember;
import com.iquanwai.confucius.biz.po.Course;
import com.iquanwai.confucius.biz.po.CourseWeek;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.util.ErrorMessageUtils;
import com.iquanwai.confucius.resolver.LoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.course.dto.CoursePageDto;
import com.iquanwai.confucius.web.course.dto.WeekIndexDto;
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
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private CourseProgressService courseProgressService;
    @Autowired
    private CourseStudyService courseStudyService;
    @Autowired
    private OperationLogService operationLogService;

    private String[] WEEK_INDEXES = {"开营前", "第一周", "第二周", "第三周"};

    @RequestMapping("/load")
    @Deprecated
    public ResponseEntity<Map<String, Object>> loadCourse(LoginUser loginUser){
        try{
            Assert.notNull(loginUser,"用户不能为空");
            ClassMember classMember = courseProgressService.loadActiveCourse(loginUser.getOpenId(), null);
            if(classMember==null){
                LOGGER.error("用户"+loginUser.getWeixinName()+"还没有报名, openid is {}", loginUser.getOpenId());
                return WebUtils.error(ErrorMessageUtils.getErrmsg("course.load.nopaid"));
            }

            int week = getProgressWeek(classMember);
            CoursePageDto course = getCourse(loginUser, classMember, week);
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
        }catch (Exception e){
            LOGGER.error("获取当前课程失败", e);
            return WebUtils.error("获取当前课程失败");
        }
    }


    @RequestMapping("/load/{courseId}")
    public ResponseEntity<Map<String, Object>> loadCourse(LoginUser loginUser, @PathVariable Integer courseId){
        try{
            Assert.notNull(loginUser,"用户不能为空");
            ClassMember classMember = courseProgressService.loadActiveCourse(loginUser.getOpenId(), courseId);
            if(classMember==null){
                LOGGER.error("用户"+loginUser.getWeixinName()+"还没有报名, openid is {}", loginUser.getOpenId());
                return WebUtils.error(ErrorMessageUtils.getErrmsg("course.load.nopaid"));
            }

            int week = getProgressWeek(classMember);
            CoursePageDto course = getCourse(loginUser, classMember, week);
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
        }catch (Exception e){
            LOGGER.error("获取当前课程失败", e);
            return WebUtils.error("获取当前课程失败");
        }
    }

    private int getProgressWeek(ClassMember classMember) {
        String personalProgress = classMember.getProgress();
        int last = 0;
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
            //TODO:11月底去掉
            if(classMember.getClassId()<7) {
                return 1;
            }
            return 0;
        }else {
            return (last - 1) / 7 + 1;
        }
    }

    private CoursePageDto getCourse(LoginUser loginUser, ClassMember classMember,
                                           int week) {

        Course course = courseProgressService.loadCourse(classMember, week);
        //设置看到某一页
        courseProgressService.personalChapterPage(loginUser.getOpenId(), course.getChapterList());
        CoursePageDto coursePageDto = new CoursePageDto();
        coursePageDto.setCourse(course);
        //加载周主题
        CourseWeek courseWeek = courseStudyService.loadCourseWeek(classMember.getCourseId(), week);
        if(courseWeek!=null) {
            coursePageDto.setWeek(week);
            coursePageDto.setTopic(courseWeek.getTopic());
        }
        //加载每周的index显示
        setWeekIndex(coursePageDto, course, classMember.getClassId());
        return coursePageDto;
    }

    // TODO: 11月底去掉参数classId
    private void setWeekIndex(CoursePageDto coursePageDto, Course course, int classId) {
        List<WeekIndexDto> weekIndexes = Lists.newArrayList();
        //hardcode，只适用于classId>=7的班级
        if(course.isPreChapter() && classId>=7){
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
        try{
            Assert.notNull(loginUser,"用户不能为空");
            ClassMember classMember = courseProgressService.loadActiveCourse(loginUser.getOpenId(), courseId);
            if(classMember==null){
                WebUtils.error("用户"+loginUser.getWeixinName()+"还没有报名");
            }

            CoursePageDto course = getCourse(loginUser, classMember, week);
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
        }catch (Exception e){
            LOGGER.error("获取用户当前课程失败", e);
            return WebUtils.error("获取用户当前课程失败");
        }
    }

    @RequestMapping("/graduate/{classId}")
    public ResponseEntity<Map<String, Object>> graduate(@PathVariable("classId") Integer classId){
        try{
            courseProgressService.graduate(classId);
            return WebUtils.success();
        }catch (Exception e){
            LOGGER.error("触发毕业失败", e);
            return WebUtils.error("触发毕业失败");
        }
    }
}
