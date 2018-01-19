package com.iquanwai.confucius.biz.domain.weixin.file;

import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;


@Service
public class WXUploadFileServiceImpl implements WXUploadFileService {
    @Autowired
    private RestfulHelper restfulHelper;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String addEverMaterial(MultipartFile multipartFile, String type, Integer tmp) throws Exception {
        String url;
        //上传临时素材
        if(tmp==1){
            url = TMP_MEDIA_UPLOAD_URL;
        }
        //上传永久素材
        else{
            url = PER_MEDIA_UPLOAD_URL;
        }
        url = url.replace("{type}",type);
        String result = restfulHelper.uploadWXFile(multipartFile,url);
        logger.info("上传微信素材的返回结果为："+result);
        return result;
    }

}
