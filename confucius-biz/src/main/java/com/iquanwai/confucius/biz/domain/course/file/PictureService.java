package com.iquanwai.confucius.biz.domain.course.file;

import com.iquanwai.confucius.biz.po.Picture;
import com.iquanwai.confucius.biz.po.PictureModule;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Created by nethunder on 2016/12/15.
 */
public interface PictureService {
    /**
     * 获取图片模块配置信息
     * @param id 模块Id
     * @return 模块配置信息
     */
    PictureModule getPictureModule(Integer id);

    /**
     * 检查图片是否有效
     * @param pictureModule 图片模块配置信息
     * @param picture {length: 图片大小，单位是字节; type: 图片类型 }
     * @return 返回值 {status:"0",error:"失败原因"},{status:"1",error:"0"}
     */
    Pair<Integer,String> checkAvaliable(PictureModule pictureModule, Picture picture);

    /**
     * 上传图片
     * @param picture 图片元数据
     * @param fileName 文件名
     * @param file 文件对象
     * */
    Map<String, Object> uploadPic(Picture picture, String fileName, MultipartFile file) throws Exception;


}
