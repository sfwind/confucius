package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.dao.fragmentation.AudioDao;
import com.iquanwai.confucius.biz.po.fragmentation.Audio;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.SFTPUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Created by 三十文 on 2017/9/22
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {
    @Autowired
    private AudioDao audioDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static String FTP_AUDIO_STORE = "/data/static/audio/";
    private final static String AUDIO_RESOURCE_PREFIX = ConfigUtils.resourceDomainName() + "/audio/";

    @Override
    public String uploadFtpAudioFile(String prefix, String originFileName, InputStream uploadFileStream) {
        SFTPUtil sftpUtil = new SFTPUtil();
        int dotIndex = originFileName.lastIndexOf(".");
        String sufFileName = originFileName.substring(dotIndex);
        String targetFileName = prefix + "_" + CommonUtils.randomString(8) + sufFileName;

        sftpUtil.upload(FTP_AUDIO_STORE, targetFileName, uploadFileStream);

        return targetFileName;
    }


    @Override
    public int uploadAudio(Integer audioId, String name, String url, String words) {
        Audio audio = new Audio();
        audio.setName(name);
        audio.setUrl(AUDIO_RESOURCE_PREFIX + url);
        audio.setWords(words);
        if(audioId==null){
            return audioDao.insertAudio(audio);
        }
        if (audioId > 0) {
            audio.setId(audioId);
            //判断是否需要更新文件路径
            Audio originAudio = audioDao.load(Audio.class, audioId);
            //如果原来是插入，则不修改Updated字段
            if (originAudio.getUpdated() == 2) {
                audio.setUpdated(originAudio.getUpdated());
            } else {
                audio.setUpdated(1);
            }
            if (StringUtils.isEmpty(url)) {
                audioDao.updateAudio(audio);
            } else {
                audioDao.updateAudioContainsUrl(audio);
            }
            return audioId;
        } else {
            return audioDao.insertAudio(audio);
        }
    }

    @Override
    public Audio loadAudio(Integer audioId) {
        return audioDao.load(Audio.class, audioId);
    }

}
