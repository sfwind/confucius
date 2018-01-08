package com.iquanwai.confucius.biz.domain.weixin.file;

import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class WXUploadFileServiceImpl implements WXUploadFileService {
    @Autowired
    private RestfulHelper restfulHelper;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 添加永久素材
     *
     * @param picFile
     * @return
     */
    @Override
    public String addEverMaterial(MultipartFile picFile,String type,Integer tmp) throws Exception {
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
        String result = restfulHelper.uploadWXFile(picFile,url);
        logger.info("上传微信素材的返回结果为："+result);
        return result;
    }

}
