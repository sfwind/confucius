package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.fragmentation.Audio;

import java.io.InputStream;

/**
 * Created by 三十文 on 2017/9/22
 */
public interface FileUploadService {
    String uploadFtpAudioFile(String prefix, String originFileName, InputStream uploadFileStream);

    int uploadAudio(Integer audioId, String name, String url, String words);

    /**
     * 保持原文件格式不变，只在名字后面添加随机参数，并且上传至七牛云
     */
    String uploadFile(String fileName, InputStream inputStream);

    Audio loadAudio(Integer audioId);
}
