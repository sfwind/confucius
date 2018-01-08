package com.iquanwai.confucius.biz.domain.weixin.file;


import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface WXUploadFileService {

    String TMP_MEDIA_UPLOAD_URL = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token={access_token}&type={type}";
    String PER_MEDIA_UPLOAD_URL = "https://api.weixin.qq.com/cgi-bin/material/add_material?access_token={access_token}&type={type}";

     String addEverMaterial(MultipartFile picFile,String type,Integer tmp) throws Exception;
}
