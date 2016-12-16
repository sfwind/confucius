package com.iquanwai.confucius.biz.domain.course.file;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.file.PictureDao;
import com.iquanwai.confucius.biz.po.Picture;
import com.iquanwai.confucius.biz.po.PictureModule;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2016/12/15.
 */
@Service
public class PictureServiceImpl implements PictureService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Map<Integer,PictureModule> moduleMap = Maps.newConcurrentMap();
    private Map<Integer,String> prefixMap = Maps.newConcurrentMap();

    @Autowired
    private PictureDao pictureDao;

    @PostConstruct
    public void initPictureModule(){
        List<PictureModule> moduleList = pictureDao.loadAll(PictureModule.class);
        if(moduleList!=null){
            moduleList.forEach(item -> {
                moduleMap.put(item.getId(), item);
                prefixMap.put(item.getId(), ConfigUtils.resourceDomainName()+"/images/"+item.getModuleName()+"/");
            });

        }

    }

    @Override
    public PictureModule getPictureModule(Integer id) {
        PictureModule pictureModule = moduleMap.get(id);
        if(pictureModule==null){
            logger.error("moduleId: {} is invalid!",id);
            return null;
        } else {
            return pictureModule;
        }
    }

    @Override
    public void reloadModule() {
        initPictureModule();
    }

    @Override
    public Map<String, String> checkAvaliable(PictureModule pictureModule, Picture picture) {
        Map<String, String> map = Maps.newHashMap();
        Integer sizeLimit = pictureModule.getSizeLimit();
        if(picture.getLength()==null){
            map.put("status","0");
            map.put("error","该图片大小未知，无法上传");
        }
        if(picture.getType()==null){
            map.put("status","0");
            map.put("error","该图片类型未知，无法上传");
            return map;
        }

        if(sizeLimit!=null && picture.getLength()>sizeLimit){
            map.put("status", "0");
            map.put("error", "该图片过大，请压缩后上传");
            return map;
        }
        List<String> typeList = pictureModule.getTypeLimit()==null? Lists.newArrayList():Lists.newArrayList(pictureModule.getTypeLimit().split(","));
        long matchTypeCount = typeList.stream().filter(contentType -> contentType.equals(picture.getType())).count();
        if(matchTypeCount==0){
            map.put("status","0");
            map.put("error",pictureModule.getModuleName()+"模块不支持该图片类型");
            return map;
        }
        // 通过校验开始上传
        map.put("status","1");
        map.put("error","0");
        return map;
    }

    @Override
    public Picture uploadPicture(PictureModule pictureModule, Integer referId, String remoteIp, String fileName, Long fileSize, String contentType, MultipartFile file) throws Exception {
        // 获取模块名对应的路径
        String path = pictureModule.getPath();
        // 文件名
        String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf("."), fileName.length()) : "";
        // 不用存储带有意义的文件名
        // fileName = fileName.substring(0, fileName.lastIndexOf(".") == -1 ? fileName.length() : fileName.lastIndexOf("."));

        // 命名规则 {module}-{date}-{rand(8)}-{referId}.{filename的后缀}
        Date today = new Date();
        String realName = pictureModule.getModuleName()+"-"+ DateUtils.parseDateToString3(today)+"-"+CommonUtils.randomString(9)+"-"+referId+suffix;
        //获取该文件的文件名
        File targetFile = new File(path, realName);
        // 不要自动创建 文件夹
        // if (!targetFile.exists()) {
        //      targetFile.mkdirs();
        // }
        // 保存
        try {
            file.transferTo(targetFile);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw e;
        }
        // 创建成功，返回Picture
        Picture picture = new Picture();
        picture.setLength(fileSize);
        picture.setModuleId(pictureModule.getId());
        picture.setReferencedId(referId);
        picture.setRemoteIp(remoteIp);
        picture.setType(contentType);
        picture.setRealName(realName);
        // 插入到数据库
        pictureDao.upload(picture);
        return picture;
    }

    @Override
    public String getModulePrefix(Integer moduleId) {
        String prefix = prefixMap.get(moduleId);
        if(prefix==null){
            logger.error("moduleId: {} is invalid",moduleId);
        }
        return prefix;
    }

    @Override
    public List<Picture> loadPicture(Integer moduleId, Integer referencedId) {

        return pictureDao.picture(moduleId, referencedId);
    }

}
