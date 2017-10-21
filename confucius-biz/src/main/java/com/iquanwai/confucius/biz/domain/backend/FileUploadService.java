package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.fragmentation.Audio;

import java.io.InputStream;

/**
 * Created by 三十文 on 2017/9/22
 */
public interface FileUploadService {
    String uploadFtpAudioFile(String prefix, String originFileName, InputStream uploadFileStream);

    int uploadAudio(Integer audioId, String name, String url, String words);

    Audio loadAudio(Integer audioId);
}
