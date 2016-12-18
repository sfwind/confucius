package com.iquanwai.confucius.web.course.controller;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.course.file.PictureService;
import com.iquanwai.confucius.biz.domain.course.progress.CourseStudyService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.HomeworkSubmit;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.Picture;
import com.iquanwai.confucius.biz.po.PictureModule;
import com.iquanwai.confucius.util.WebUtils;
import okhttp3.MultipartBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Created by nethunder on 2016/12/14.
 */
@RestController
@RequestMapping("/file")
public class FileController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private CourseStudyService courseStudyService;

    /**
     * 上传图片
     * @param moduleId 图片的模块
     * @param referId 图片依赖的id
     * @param file 上传的文件
     * @return 响应
     */
    @RequestMapping(value = "/image/upload/{moduleId}/{referId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> imageUpload(@PathVariable("moduleId") Integer moduleId,
                                                           @PathVariable("referId") Integer referId,
                                                           @RequestParam("file") MultipartFile file,
                                                           HttpServletRequest request
    ) {
        try {
            if (moduleId != null && file != null && !file.isEmpty()) {
                String fileName = file.getOriginalFilename();
                Long fileSize = file.getSize();
                String contentType = file.getContentType();
                String remoteIp = request.getHeader("X-Forwarded-For");
                PictureModule pictureModule = pictureService.getPictureModule(moduleId);
                Picture picture = new Picture(fileSize, referId, contentType, remoteIp);
                if (pictureModule != null) {
                    Pair<Integer,String> checkResult = pictureService.checkAvaliable(pictureModule, picture);
                    if (checkResult.getLeft() == 1) {
                        // 可上传
                        try {
                            HomeworkSubmit submit = courseStudyService.loadMemberSubmittedHomework(referId);
                            OperationLog operationLog = OperationLog.create().openid(submit.getSubmitOpenid())
                                    .module("文件")
                                    .function("上传图片")
                                    .action("PC上传图片")
                                    .memo(moduleId + "");
                            operationLogService.log(operationLog);
                            picture = pictureService.uploadPicture(pictureModule, referId, remoteIp, fileName, fileSize, contentType, file);
                        } catch (FileNotFoundException e) {
                            LOGGER.error("upload image error:上传图片失败，模块目录:" + pictureModule.getModuleName() + "未创建!", e);
                            return WebUtils.error("该模块目录未创建");
                        } catch (Exception e) {
                            LOGGER.error("upload image error:上传图片失败", e);
                            return WebUtils.error(e.getLocalizedMessage());
                        }
                        String url = pictureService.getModulePrefix(moduleId) + picture.getRealName();
                        Map<String, Object> map = Maps.newHashMap();
                        map.put("picUrl", url);
                        return WebUtils.result(map);
                    } else {
                        // 不可上传
                        LOGGER.error("upload image error:上传校验失败," + checkResult.getRight());
                        return WebUtils.error(checkResult.getRight());
                    }
                } else {
                    LOGGER.error("upload image error:无该图片模块," + moduleId);
                    return WebUtils.error("无该模块:+" + moduleId);
                }
            } else {
                // 模块名为空,禁止上传
                if (moduleId == null) {
                    LOGGER.error("upload image error:moduleId为空");
                    return WebUtils.error("请求异常，请重试");
                } else {
                    LOGGER.error("upload image error:文件解析失败");
                    return WebUtils.error("该图片无法解析，请重新选择");
                }
            }
        } catch (Exception e){
            LOGGER.error("upload image error:上传图片失败", e);
            return WebUtils.error("上传图片失败");
        }
    }
}