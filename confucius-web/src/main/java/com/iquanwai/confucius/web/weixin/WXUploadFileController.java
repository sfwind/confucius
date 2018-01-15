package com.iquanwai.confucius.web.weixin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.file.WXMediaService;
import com.iquanwai.confucius.biz.domain.weixin.file.WXUploadFileService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller
@RequestMapping("/wx/file")
public class WXUploadFileController {
    @Autowired
    private WXUploadFileService wxUploadFileService;
    @Autowired
    private WXMediaService wxMediaService;
    @Autowired
    private OperationLogService operationLogService;


    /**
     * 上传临时微信素材
     *
     * @param picFile
     * @param type
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/upload/{type}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> uploadWXFile(PCLoginUser loginUser, @PathVariable String type, @RequestParam(value = "file") MultipartFile picFile, @RequestParam("tmp") Integer tmp, @RequestParam("remark") String remark) throws Exception {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("后台管理").function("上传微信素材").action("上传微信素材");
        operationLogService.log(operationLog);
        if (picFile != null) {
            String result = wxUploadFileService.addEverMaterial(picFile.getInputStream(), picFile.getOriginalFilename(), type, tmp);
            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.containsKey("errcode")) {
                return WebUtils.error("上传图片失败");
            }
            String media_id = jsonObject.getString("media_id");
            String url = null;
            if (jsonObject.containsKey("url")) {
                url = jsonObject.getString("url");
            }
            if(wxMediaService.insertMediaId(media_id, url, remark)>0){
                return WebUtils.result(media_id);
            }
            return WebUtils.error("存入数据库失败");
        } else {
            return WebUtils.error("上传图片为空");
        }
    }
}
