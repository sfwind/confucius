package com.iquanwai.confucius.web.course.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.ClassMember;
import com.iquanwai.confucius.biz.po.Course;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.resolver.LoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.course.dto.CoursePageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
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
    private OperationLogService operationLogService;

    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> loadCourse(LoginUser loginUser){
        try{
            Assert.notNull(loginUser,"用户不能为空");
            ClassMember classMember = courseProgressService.loadActiveCourse(loginUser.getOpenId(), null);
            if(classMember==null){
                WebUtils.error(200, "用户"+loginUser.getWeixinName()+"还没有报名");
            }

            CoursePageDto course = getCourse(loginUser, classMember, classMember.getProgressWeek());
            if(course==null){
                return WebUtils.error(200, "获取用户当前课程失败");
            }
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("课程")
                    .function("学习课程")
                    .action("打开课程引导页")
                    .memo(course.getCourse().getId()+"");
            operationLogService.log(operationLog);
            return WebUtils.result(course);
        }catch (Exception e){
            LOGGER.error("获取用户当前课程失败", e);
            return WebUtils.error(200, "获取用户当前课程失败");
        }
    }

    private CoursePageDto getCourse(LoginUser loginUser, ClassMember classMember,
                                           int courseWeek) {
        List<Integer> personalProgressList = Lists.newArrayList();
        String personalProgress = classMember.getProgress();
        if(personalProgress!=null) {
            String[] progressArr = personalProgress.split(",");
            for(int i=0;i<progressArr.length;i++){
                try {
                    personalProgressList.add(Integer.valueOf(progressArr[i]));
                }catch (NumberFormatException e){
                    LOGGER.error(loginUser.getOpenId()+" progress is abnormal,"+progressArr[i]+" is not a number");
                }
            }
        }

        Course course = courseProgressService.loadCourse(classMember.getCourseId(),courseWeek,
                personalProgressList, classMember.getClassProgress());

        CoursePageDto coursePageDto = new CoursePageDto();
        coursePageDto.setCourse(course);
        coursePageDto.setUsername(loginUser.getWeixinName());
        coursePageDto.setOpenid(loginUser.getOpenId());
        coursePageDto.setWeek(classMember.getProgressWeek());
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
                WebUtils.error(200, "用户"+loginUser.getWeixinName()+"还没有报名");
            }

            CoursePageDto course = getCourse(loginUser, classMember, week);
            if(course==null){
                return WebUtils.error(200, "获取用户当前课程失败");
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
            return WebUtils.error(200, "获取用户当前课程失败");
        }
    }
}
