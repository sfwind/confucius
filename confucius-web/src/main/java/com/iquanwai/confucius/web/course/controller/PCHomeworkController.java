package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.domain.course.file.PictureService;
import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.Picture;
import com.iquanwai.confucius.biz.po.systematism.Homework;
import com.iquanwai.confucius.biz.po.systematism.HomeworkSubmit;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.web.course.dto.HomeworkLoadDto;
import com.iquanwai.confucius.web.course.dto.HomeworkSubmitDto;
import com.iquanwai.confucius.web.course.dto.PictureDto;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/9/3.
 */
@RestController
@RequestMapping("/homework")
public class PCHomeworkController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private CourseStudyService courseStudyService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PictureService pictureService;

    @RequestMapping("/load/{url}")
    public ResponseEntity<Map<String, Object>> loadHomework(@PathVariable("url") String url){
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
        Homework homework = courseStudyService.loadHomework(submit.getSubmitProfileId(), submit.getHomeworkId());
        if(homework==null){
            return WebUtils.error("获取作业失败");
        }
        // 加载大作业的图片
        List<Picture> pictureList = pictureService.loadPicture(Constants.PictureType.HOMEWORK, submit.getId());

        HomeworkLoadDto homeworkLoadDto = new HomeworkLoadDto(Constants.PictureType.HOMEWORK, submit.getId(),
                homework, pictureList.stream().map(item -> {
            String picUrl = pictureService.getModulePrefix(Constants.PictureType.HOMEWORK) + item.getRealName();
            return new PictureDto(Constants.PictureType.HOMEWORK, submit.getId(), picUrl);
        }).collect(Collectors.toList()));

        return WebUtils.result(homeworkLoadDto);
    }

    @RequestMapping(value="/submit/{url}", method= RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submit(@PathVariable("url") String url,
                                                      @RequestBody HomeworkSubmitDto homeworkSubmitDto){
        String completeUrl = "/static/h?id="+url;
        HomeworkSubmit submit = courseStudyService.loadHomework(completeUrl);

        if(submit==null){
            return WebUtils.error("提交作业失败");
        }
        if(StringUtils.isEmpty(homeworkSubmitDto.getAnswer())){
            return WebUtils.error("请写完后再提交");
        }
        if(homeworkSubmitDto.getAnswer().length()>10000){
            return WebUtils.error("字数太长，请删减到10000字以下");
        }
        String openid = submit.getSubmitOpenid();
        courseStudyService.submitHomework(homeworkSubmitDto.getAnswer(), submit.getSubmitProfileId(), submit.getHomeworkId());

        OperationLog operationLog = OperationLog.create().openid(openid)
                .module("作业")
                .function("做作业")
                .action("PC提交作业")
                .memo(submit.getHomeworkId()+"");
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping("/load/{homeworkId}/all")
    public ResponseEntity<Map<String, Object>> loadHomeworkAll(@PathVariable("homeworkId") Integer homeworkId){
        List<HomeworkSubmit> submit = courseStudyService.loadSubmittedHomework(homeworkId);

        return WebUtils.result(submit);
    }

}
