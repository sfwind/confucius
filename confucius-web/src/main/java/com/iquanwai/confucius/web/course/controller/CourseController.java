package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.dao.po.ClassMember;
import com.iquanwai.confucius.biz.dao.po.Course;
import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.resolver.LoginUser;
import com.iquanwai.confucius.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Created by justin on 16/8/25.
 */
@Controller
@RequestMapping("/course")
public class CourseController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private CourseProgressService courseProgressService;

    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> loadCourse(LoginUser loginUser){
        try{
            Assert.notNull(loginUser,"用户不能为空");
            ClassMember classMember = courseProgressService.loadActiveCourse(loginUser.getOpenId(), null);
            if(classMember==null){
                WebUtils.error(200, "用户"+loginUser.getWeixinName()+"还没有报名");
            }
            Course course = courseProgressService.loadCourse(classMember.getCourseId(), classMember.getProgressWeek(),
                    classMember.getProgress(), classMember.getClassProgress());
            return WebUtils.result(course);
        }catch (Exception e){
            LOGGER.error("获取用户当前课程失败", e);
            return WebUtils.error(200, "获取用户当前课程失败");
        }
    }

    @RequestMapping("/week/{courseId}/{week}")
    public ResponseEntity<Map<String, Object>> loadWeek(@PathVariable("courseId") int courseId,
                                                        @PathVariable("week") int week,
                                                        LoginUser loginUser){
        try{
            Assert.notNull(loginUser,"用户不能为空");
            ClassMember classMember = courseProgressService.loadActiveCourse(loginUser.getOpenId(), courseId);
            if(classMember==null){
                WebUtils.error(200, "用户"+loginUser.getWeixinName()+"还没有报名");
            }
            Course course = courseProgressService.loadCourse(classMember.getCourseId(), week,
                    classMember.getProgress(), classMember.getClassProgress());
            return WebUtils.result(course);
        }catch (Exception e){
            LOGGER.error("获取用户当前课程失败", e);
            return WebUtils.error(200, "获取用户当前课程失败");
        }
    }
}
