package com.iquanwai.confucius.biz.domain.weixin.file;

import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.InputStream;


@Service
public class WXUploadFileServiceImpl implements WXUploadFileService {
    @Autowired
    private RestfulHelper restfulHelper;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 添加微信素材
     *
     * @return
     */
    @Override
    public String addEverMaterial(InputStream inputStream,String fileName, String type, Integer tmp) throws Exception {
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
        String result = restfulHelper.uploadWXFile(inputStream,fileName,url);
        logger.info("上传微信素材的返回结果为："+result);
        return result;
    }

}
