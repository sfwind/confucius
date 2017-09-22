package com.iquanwai.confucius.biz.domain.backend;

import java.io.InputStream;

/**
 * Created by 三十文 on 2017/9/22
 */
public interface FileUploadService {
    String uploadFtpAudioFile(String prefix, String originFileName, InputStream uploadFileStream);

    int insertAudio(String name, String url, String words);
}
