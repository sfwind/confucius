package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.domain.course.operational.OperationalService;
import com.iquanwai.confucius.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by justin on 16/10/8.
 */
@RestController
@RequestMapping("/b")
public class BackendController {
    @Autowired
    private OperationalService operationalService;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @RequestMapping("/assign/angel/{classId}")
    public ResponseEntity<Map<String, Object>> submit(@PathVariable("classId") Integer classId){
        boolean result = false;
        try {
            result = operationalService.angelAssign(classId);
        }catch (Exception e){
            LOGGER.error("分配天使失败", e);
        }

        return WebUtils.result(result);
    }
}
