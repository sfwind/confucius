package com.iquanwai.confucius.web.course.controller;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.course.file.PictureService;
import com.iquanwai.confucius.biz.po.Picture;
import com.iquanwai.confucius.biz.po.PictureModule;
import com.iquanwai.confucius.util.WebUtils;
import okhttp3.MultipartBody;
import org.apache.commons.lang3.StringUtils;
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
    private void log(Object obj){
        System.out.println(obj);
    }

    @Autowired
    private PictureService pictureService;

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
        /**
         * 记得把这个C加到拦截器排除配置里
         *
         * 模块表：模块Id，模块名，相对地址，是否有缩略图，大小限制，类型限制
         *      Id,ModuleName,Path,HasThumbnail,SizeLimit,TypeLimit
         * 图片表：,根据ModuleName查询出数据
         *      图片Id，模块，依赖id，上传Ip？ ，不重复的唯一文件名，图片大小，图片类型,缩略图地址
         *      Id,ModuleName(ModuleId),ReferId,Ip,RealName,Length,Type,Thumbnail
         */
        if(moduleId!=null && file!=null && !file.isEmpty()){
            String fileName = file.getOriginalFilename();
            Long fileSize = file.getSize();
            String contentType = file.getContentType();
            String remoteIp = request.getHeader("X-Forwarded-For");
            System.out.println(remoteIp);
            PictureModule pictureModule = pictureService.getPictureModule(moduleId);
            Picture picture = new Picture(fileSize,referId,contentType,remoteIp);
            if(pictureModule!=null){
                Map<String, String> checkMap = pictureService.checkAvaliable(pictureModule, picture);
                if("1".equals(checkMap.get("status"))){
                    // 可上传
                    try{
                        picture = pictureService.uploadPicture(pictureModule,referId,remoteIp,fileName,fileSize,contentType,file);
                    } catch (FileNotFoundException e){
                        return WebUtils.error("该模块目录未创建");
                    } catch (Exception e){
                        return WebUtils.error(e.getLocalizedMessage());
                    }
                    String url = pictureService.getModulePrefix(moduleId)+picture.getRealName();
                    Map<String,Object> map = Maps.newHashMap();
                    map.put("picUrl",url);
                    return WebUtils.result(map);
                } else {
                    // 不可上传
                    return WebUtils.error(checkMap.get("error"));
                }
            } else {
                return WebUtils.error("无该模块:+" + moduleId);
            }
        } else {
            // 模块名为空,禁止上传
            if(moduleId==null){
                return WebUtils.error("请求异常，请重试");
            } else {
                return WebUtils.error("该图片无法解析，请重新选择");
            }
        }
    }


}
