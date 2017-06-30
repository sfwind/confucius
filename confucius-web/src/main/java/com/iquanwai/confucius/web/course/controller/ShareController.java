package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.domain.course.progress.CourseProgressService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.systematism.ClassMember;
import com.iquanwai.confucius.biz.po.systematism.Course;
import com.iquanwai.confucius.web.course.dto.CertificateDto;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by justin on 16/11/2.
 */
@RestController
@RequestMapping("/share")
public class ShareController {
    @Autowired
    private CourseProgressService courseProgressService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AccountService accountService;

    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @RequestMapping("/certificate/{certificateNo}")
    public ResponseEntity<Map<String, Object>> certificateInfo(@PathVariable("certificateNo") String certificateNo){
        CertificateDto certificateDto = new CertificateDto();
        ClassMember classMember = courseProgressService.loadClassMemberByCertificateNo(certificateNo);
        if(classMember!=null){
            certificateDto.setCertificateNo(classMember.getCertificateNo());
            Course course = courseProgressService.loadCourse(classMember.getCourseId());
            if(course!=null){
                certificateDto.setCertificateBg(course.getCertificatePic());
                certificateDto.setComment(courseProgressService.certificateComment(course.getName(), classMember));
            }
            //获取真名
            Profile account = accountService.getProfile(classMember.getProfileId());
            if(account!=null){
                certificateDto.setName(account.getRealName());
            }
            OperationLog operationLog = OperationLog.create().openid(classMember.getOpenId())
                    .module("证书")
                    .function("分享")
                    .action("打开证书")
                    .memo(classMember.getMemberId()+"");
            operationLogService.log(operationLog);
        }

        return WebUtils.result(certificateDto);
    }
}
