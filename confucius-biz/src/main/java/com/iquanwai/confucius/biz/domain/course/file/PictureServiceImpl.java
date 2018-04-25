package com.iquanwai.confucius.biz.domain.course.file;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.common.file.PictureDao;
import com.iquanwai.confucius.biz.po.Picture;
import com.iquanwai.confucius.biz.po.PictureModule;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.QiNiuUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2016/12/15.
 */
@Service
public class PictureServiceImpl implements PictureService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Map<Integer, PictureModule> moduleMap = Maps.newConcurrentMap();
    private Map<Integer, String> prefixMap = Maps.newConcurrentMap();

    private static final Long MAX_PIC_SIZE = 10485760L;

    @Autowired
    private PictureDao pictureDao;

    @PostConstruct
    public void initPictureModule() {
        List<PictureModule> moduleList = pictureDao.loadAll(PictureModule.class);
        if (moduleList != null) {
            moduleList.forEach(item -> {
                moduleMap.put(item.getId(), item);
                prefixMap.put(item.getId(), ConfigUtils.getUploadDomain() + "/images/" + item.getModuleName() + "/");
            });
        }

    }

    @Override
    public PictureModule getPictureModule(Integer id) {
        PictureModule pictureModule = moduleMap.get(id);
        if (pictureModule == null) {
            logger.error("moduleId: {} is invalid!", id);
            return null;
        } else {
            return pictureModule;
        }
    }

    @Override
    public Pair<Integer, String> checkAvaliable(PictureModule pictureModule, Picture picture) {
        if (picture.getLength() == null) {
            return Pair.of(0, "该图片大小未知，无法上传");
        }
        if (picture.getType() == null) {
            return Pair.of(0, "该图片类型未知，无法上传");
        }

        if (picture.getLength() > MAX_PIC_SIZE) {
            return Pair.of(0, "该图片过大，请压缩后上传");
        }
        List<String> typeList = pictureModule.getTypeLimit() == null ?
                Lists.newArrayList() : Lists.newArrayList(pictureModule.getTypeLimit().split(","));
        long matchTypeCount = typeList.stream().filter(contentType -> contentType.equals(picture.getType())).count();
        if (matchTypeCount == 0) {
            return Pair.of(0, pictureModule.getModuleName() + "模块不支持该图片类型");
        }
        // 通过校验开始上传
        return Pair.of(1, null);
    }


    @Override
    public Map<String, Object> uploadPic(Picture picture, String fileName, MultipartFile file) throws Exception {
        Map<String, Object> resultMap = Maps.newHashMap();
        String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf("."), fileName.length()) : "";
        PictureModule pictureModule = moduleMap.get(picture.getModuleId());
        String realName = pictureModule.getModuleName() + "-" +
                DateUtils.parseDateToString3(new Date()) + "-" + CommonUtils.randomString(9) + suffix;
        picture.setRealName(realName);
        String url = ConfigUtils.getPicturePrefix() + realName;

        int id = pictureDao.upload(picture);
        boolean result;
        try {
            result = QiNiuUtils.uploadFile(realName, file.getInputStream());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw e;
        }

        resultMap.put("result", result);
        resultMap.put("picUrl", url);
        resultMap.put("picId", id);
        return resultMap;
    }

}
