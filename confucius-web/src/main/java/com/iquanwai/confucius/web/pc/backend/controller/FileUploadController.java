package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.backend.FileUploadService;
import com.iquanwai.confucius.web.pc.backend.dto.AudioUploadDto;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Created by 三十文 on 2017/9/22
 */
@RestController
@RequestMapping("/pc/upload")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/audio/ftp")
    public ResponseEntity<Map<String, Object>> uploadAudio(@RequestParam("prefix") String prefix, @RequestParam("file") MultipartFile file) throws IOException {
        Long fileSize = file.getSize();
        if (fileSize > 10 * 1000 * 1000) { // 文件图片大于 10M
            return WebUtils.error("文件大小超过 10 M");
        }

        String fileName = file.getOriginalFilename();
        int dotIndex = fileName.lastIndexOf(".");

        String sufFileName = fileName.substring(dotIndex);
        if (dotIndex < 0) {
            return WebUtils.error("未知文件类型");
        }
        if (!".m4a".equals(sufFileName)) {
            return WebUtils.error("非指定格式音频");
        }

        String ftpFileName = fileUploadService.uploadFtpAudioFile(prefix, fileName, file.getInputStream());
        if (ftpFileName != null) {
            return WebUtils.result(ftpFileName);
        } else {
            return WebUtils.error("音频上传失败");
        }
    }

    @RequestMapping("/audio/db")
    public ResponseEntity<Map<String, Object>> insertAudioData(@RequestBody AudioUploadDto audioUploadDto) {
        String name = audioUploadDto.getName();
        String ftpFileName = audioUploadDto.getFtpFileName();
        String words = audioUploadDto.getWords();

        int result = fileUploadService.insertAudio(name, ftpFileName, words);
        if (result > 0) {
            return WebUtils.success();
        } else {
            return WebUtils.error("保存音频数据失败");
        }
    }

}
