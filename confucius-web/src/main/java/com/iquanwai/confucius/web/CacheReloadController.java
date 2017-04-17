package com.iquanwai.confucius.web;

import com.iquanwai.confucius.biz.domain.course.file.PictureService;
import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.course.signup.RiseMemberCountRepo;
import com.iquanwai.confucius.biz.domain.course.signup.RiseMemberTypeRepo;
import com.iquanwai.confucius.biz.domain.course.signup.SignupService;
import com.iquanwai.confucius.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.confucius.biz.domain.permission.PermissionService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.web.util.WebUtils;
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
    @Autowired
    private PictureService pictureService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private PointRepo pointRepo;
    @Autowired
    private AccountService accountService;
    @Autowired
    private RiseMemberTypeRepo riseMemberTypeRepo;
    @Autowired
    private RiseMemberCountRepo riseMemberCountRepo;


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

    @RequestMapping("/file/module/reload")
    public ResponseEntity<Map<String,Object>> fileModuleReload(){
        try{
            pictureService.reloadModule();
            return WebUtils.success();
        } catch(Exception e){
            LOGGER.error("reload file module info",e);
        }
        return WebUtils.error("reload module file");
    }

    @RequestMapping("/permission/reload")
    public ResponseEntity<Map<String,Object>> reloadPermission(){
        try{
            permissionService.reloadPermission();
            return WebUtils.success();
        } catch (Exception e){
            LOGGER.error("reload permission error",e);
        }
        return WebUtils.error("reload permission");
    }

    @RequestMapping("/score/reload")
    public ResponseEntity<Map<String,Object>> reloadScore(){
        try{
            pointRepo.reloadScore();
            return WebUtils.success();
        } catch (Exception e){
            LOGGER.error("reload score error", e);
        }
        return WebUtils.error("reload score");
    }

    @RequestMapping("/region/reload")
    public ResponseEntity<Map<String,Object>> reloadRegion(){
        try{
            accountService.loadAllProvinces();
            accountService.loadCities();
            return WebUtils.success();
        } catch (Exception e){
            return WebUtils.error("reload region");
        }
    }

    @RequestMapping("/membertype/reload")
    public ResponseEntity<Map<String,Object>> memberTypeReload(){
        try{
            riseMemberTypeRepo.reload();
            return WebUtils.success();
        } catch (Exception e){
            return WebUtils.error("reload region");
        }
    }

    @RequestMapping("/rise/member/reload")
    public ResponseEntity<Map<String,Object>> riseMemberReload(){
        try{
            riseMemberCountRepo.reload();
            return WebUtils.success();
        } catch (Exception e){
            return WebUtils.error("reload riseMember error");
        }
    }
}
