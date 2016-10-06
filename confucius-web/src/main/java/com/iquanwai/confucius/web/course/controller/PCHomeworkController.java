package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.Homework;
import com.iquanwai.confucius.biz.po.HomeworkSubmit;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.course.dto.HomeworkReviewDto;
import com.iquanwai.confucius.web.course.dto.HomeworkSubmitDto;
import com.iquanwai.confucius.web.course.dto.PCHomeworkDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/9/3.
 */
@RestController
@RequestMapping("/homework")
public class PCHomeworkController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private CourseStudyService courseStudyService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping("/load/{url}")
    public ResponseEntity<Map<String, Object>> loadHomework(@PathVariable("url") String url){
        try{
            String completeUrl = "/static/h?id="+url;
            HomeworkSubmit submit = courseStudyService.loadHomework(completeUrl);

            if(submit==null){
                return WebUtils.error("获取作业失败");
            }
            String openid = submit.getSubmitOpenid();
            OperationLog operationLog = OperationLog.create().openid(openid)
                    .module("作业")
                    .function("做作业")
                    .action("PC加载作业")
                    .memo(submit.getHomeworkId()+"");
            operationLogService.log(operationLog);
            Homework homework = courseStudyService.loadHomework(openid, submit.getHomeworkId());
            if(homework==null){
                return WebUtils.error("获取作业失败");
            }
            PCHomeworkDto pcHomeworkDto = new PCHomeworkDto();
            pcHomeworkDto.setOpenid(openid);
            pcHomeworkDto.setHomework(homework);
            return WebUtils.result(pcHomeworkDto);
        }catch (Exception e){
            LOGGER.error("获取作业失败", e);
            return WebUtils.error("获取作业失败");
        }
    }

    @RequestMapping(value="/submit/{url}", method= RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submit(@PathVariable("url") Integer url,
            @RequestBody HomeworkSubmitDto homeworkSubmitDto){
        try{
            String completeUrl = "/static/h?id="+url;
            HomeworkSubmit submit = courseStudyService.loadHomework(completeUrl);

            if(submit==null){
                return WebUtils.error("提交作业失败");
            }
            String openid = submit.getSubmitOpenid();
            courseStudyService.submitHomework(homeworkSubmitDto.getAnswer(), openid, submit.getHomeworkId());

            OperationLog operationLog = OperationLog.create().openid(openid)
                    .module("作业")
                    .function("做作业")
                    .action("PC提交作业")
                    .memo(submit.getHomeworkId()+"");
            operationLogService.log(operationLog);
            return WebUtils.success();
        }catch (Exception e){
            LOGGER.error("提交作业失败", e);
            return WebUtils.error("提交作业失败");
        }
    }

    @RequestMapping("/load/{homeworkId}/all")
    public ResponseEntity<Map<String, Object>> loadHomeworkAll(@PathVariable("homeworkId") Integer homeworkId){
        try{
            List<HomeworkSubmit> submit = courseStudyService.loadSubmittedHomework(homeworkId);

            return WebUtils.result(submit);
        }catch (Exception e){
            LOGGER.error("获取作业失败", e);
            return WebUtils.error("获取作业失败");
        }
    }

    @RequestMapping(value = "/remark", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> remark(@RequestBody HomeworkReviewDto homeworkReviewDto){
        try{
            courseStudyService.remark(homeworkReviewDto.getOpenid(),
                    homeworkReviewDto.getClassId(),
                    homeworkReviewDto.getHomeworkId(),
                    homeworkReviewDto.isExcellent(),
                    homeworkReviewDto.isFail());

            return WebUtils.success();
        }catch (Exception e){
            LOGGER.error("批改作业失败", e);
            return WebUtils.error("批改作业失败");
        }
    }
}
