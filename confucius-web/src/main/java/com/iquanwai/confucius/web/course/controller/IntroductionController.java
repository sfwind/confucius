package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.dao.po.ClassMember;
import com.iquanwai.confucius.biz.dao.po.Course;
import com.iquanwai.confucius.biz.dao.po.OperationLog;
import com.iquanwai.confucius.biz.domain.course.introduction.CourseIntroductionService;
import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.resolver.LoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.course.dto.MyCourseDto;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/9/4.
 */
@Controller
@RequestMapping("/introduction")
public class IntroductionController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private CourseProgressService courseProgressService;
    @Autowired
    private CourseIntroductionService courseIntroductionService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping("/mycourse")
    public ResponseEntity<Map<String, Object>> mycourse(LoginUser loginUser){
        try{
            Assert.notNull(loginUser, "用户不能为空");
            MyCourseDto courseDto = new MyCourseDto();
            ClassMember classMember = courseProgressService.loadActiveCourse(loginUser.getOpenId(), null);
            if(classMember==null){
                OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                        .module("服务号")
                        .function("介绍")
                        .action("训练营");
                operationLogService.log(operationLog);
                return WebUtils.result(courseDto);
            }
            Course course = courseIntroductionService.loadCourse(classMember.getCourseId());
            if(course==null){
                return WebUtils.error(200, "获取介绍失败");
            }
            courseDto.setOpenid(loginUser.getOpenId());
            courseDto.setUsername(loginUser.getWeixinName());
            courseDto.setCourse(course);
            courseDto.setCourseProgress(courseProgress(course, classMember));
            courseDto.setMyProgress(myProgress(course, classMember));
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("服务号")
                    .function("介绍")
                    .action("我的训练");
            operationLogService.log(operationLog);
            return WebUtils.result(courseDto);
        }catch (Exception e){
            LOGGER.error("获取介绍失败", e);
            return WebUtils.error(200, "获取介绍失败");
        }
    }

    private Double courseProgress(Course course, ClassMember classMember) {
        Assert.notNull(course, "课程不能为空");
        Assert.notNull(classMember, "班级不能为空");
        return classMember.getClassProgress()*1.0/course.getLength();
    }

    private Double myProgress(Course course, ClassMember classMember) {
        Assert.notNull(course, "课程不能为空");
        Assert.notNull(classMember, "班级不能为空");
        String progress = classMember.getProgress();
        if(StringUtils.isEmpty(progress)){
            return 0.0;
        }

        String[] chapterArr = progress.split(",");
        Integer largest = 1;
        for(String chapter:chapterArr){
            if(Integer.valueOf(chapter)>largest){
                largest = Integer.valueOf(chapter);
            }
        }

        return largest*1.0/course.getLength();
    }

    @RequestMapping("/allcourse")
    public ResponseEntity<Map<String, Object>> allcourse(LoginUser loginUser){


        try{
            Assert.notNull(loginUser, "用户不能为空");
            List<Course> courseList = courseIntroductionService.loadAll();
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("服务号")
                    .function("介绍")
                    .action("更多训练");
            operationLogService.log(operationLog);
            return WebUtils.result(courseList);
        }catch (Exception e){
            LOGGER.error("获取更多训练失败", e);
            return WebUtils.error(200, "获取更多训练失败");
        }
    }
}
