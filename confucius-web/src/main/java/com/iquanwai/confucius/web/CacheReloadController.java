package com.iquanwai.confucius.web;

import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Created by justin on 16/10/7.
 */
@Controller
@RequestMapping("/cache")
public class CacheReloadController {
    @Autowired
    private CourseStudyService courseStudyService;
    @Autowired
    private SignupService signupService;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @RequestMapping("/question/reload")
    public ResponseEntity<Map<String, Object>> questionReload() {
        try {
            courseStudyService.reloadQuestion();
            return WebUtils.success();
        }catch (Exception e){
            LOGGER.error("reload question", e);
        }
        return WebUtils.error("reload question");
    }

    @RequestMapping("/class/reload")
    public ResponseEntity<Map<String, Object>> classReload() {
        try {
            signupService.reloadClass();
            return WebUtils.success();
        }catch (Exception e){
            LOGGER.error("reload class", e);
        }
        return WebUtils.error("reload class");
    }
}