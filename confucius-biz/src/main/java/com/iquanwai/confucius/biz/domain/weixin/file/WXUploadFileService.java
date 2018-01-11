package com.iquanwai.confucius.biz.domain.weixin.file;

import java.io.InputStream;

public interface WXUploadFileService {

    String TMP_MEDIA_UPLOAD_URL = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token={access_token}&type={type}";
    String PER_MEDIA_UPLOAD_URL = "https://api.weixin.qq.com/cgi-bin/material/add_material?access_token={access_token}&type={type}";

    /**
     * 新增素材
     * @param inputStream（输入流）
     * @param fileName（文件名）
     * @param type（文件类型）
     * @param tmp（1为永久素材，0为临时素材）
     * @return
     * @throws Exception
     */
     String addEverMaterial(InputStream inputStream, String fileName,String type, Integer tmp) throws Exception;
}
