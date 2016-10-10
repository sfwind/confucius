package com.iquanwai.confucius.web.course.controller;

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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping("/load")
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

    private int getProgressWeek(ClassMember classMember) {
        String personalProgress = classMember.getProgress();
        int last = 0;
        if(StringUtils.isNotEmpty(personalProgress)) {
            String[] progressArr = personalProgress.split(",");
            for(int i=0;i<progressArr.length;i++){
                try {
                    int that = Integer.valueOf(progressArr[i]);
                    if(that>last){
                        last = that;
                    }
                }catch (NumberFormatException e){
                    LOGGER.error(classMember.getOpenId()+" progress is abnormal,"+progressArr[i]+" is not a number");
                }
            }
        }

        return last/7+1;
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
        coursePageDto.setWeek(week);
        coursePageDto.setTopic(courseWeek.getTopic());
        return coursePageDto;
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
