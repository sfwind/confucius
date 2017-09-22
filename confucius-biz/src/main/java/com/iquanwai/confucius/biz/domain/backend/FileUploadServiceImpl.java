package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.dao.fragmentation.AudioDao;
import com.iquanwai.confucius.biz.po.fragmentation.Audio;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 三十文 on 2017/9/22
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private AudioDao audioDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final String FTP_AUDIO_STORE = "/data/static/audio/";
    private final String AUDIO_RESOURCE_PREFIX = ConfigUtils.resourceDomainName() + "/audio/";

    @Override
    public String uploadFtpAudioFile(String prefix, String originFileName, InputStream uploadFileStream) {
        FTPUtil ftpUtil = new FTPUtil();
        int dotIndex = originFileName.lastIndexOf(".");
        String sufFileName = originFileName.substring(dotIndex);
        String targetFileName = prefix + "_" + CommonUtils.randomString(8) + sufFileName;
        try {
            ftpUtil.connect();
            boolean result = ftpUtil.storeFile(FTP_AUDIO_STORE + targetFileName, uploadFileStream);
            if (result) {
                return targetFileName;
            }
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    @Override
    public int insertAudio(String name, String ftpFileName, String words) {
        Audio audio = new Audio();
        audio.setName(name);
        audio.setUrl(AUDIO_RESOURCE_PREFIX + ftpFileName);
        audio.setWords(words);
        return audioDao.insertAudio(audio);
    }

}
