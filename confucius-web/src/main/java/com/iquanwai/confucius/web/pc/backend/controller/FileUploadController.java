package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.backend.FileUploadService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.Audio;
import com.iquanwai.confucius.web.pc.backend.dto.AudioUploadDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping("/audio/ftp")
    public ResponseEntity<Map<String, Object>> uploadAudio(PCLoginUser loginUser,
                                                           @RequestParam("prefix") String prefix,
                                                           @RequestParam("file") MultipartFile file) throws IOException {
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
            return WebUtils.error("请上传m4a格式的语音");
        }
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("上传语音")
                .action("上传语音");
        operationLogService.log(operationLog);
        String ftpFileName = fileUploadService.uploadFtpAudioFile(prefix, fileName, file.getInputStream());
        if (ftpFileName != null) {
            return WebUtils.result(ftpFileName);
        } else {
            return WebUtils.error("音频上传失败");
        }
    }

    @RequestMapping("/audio/db")
    public ResponseEntity<Map<String, Object>> insertAudioData(PCLoginUser loginUser, @RequestBody AudioUploadDto audioUploadDto) {
        String name = audioUploadDto.getName();
        String ftpFileName = audioUploadDto.getFtpFileName();
        String words = audioUploadDto.getWords();
        Integer audioId = audioUploadDto.getAudioId();
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("上传语音")
                .action("添加语音文稿");
        operationLogService.log(operationLog);
        int result = fileUploadService.uploadAudio(audioId, name, ftpFileName, words);
        if (result > 0) {
            return WebUtils.result(result);
        } else {
            return WebUtils.error("保存音频数据失败");
        }
    }

    @RequestMapping("/audio/load/{audioId}")
    public ResponseEntity<Map<String, Object>> loadAudio(PCLoginUser loginUser, @PathVariable Integer audioId) {

        Audio audio = fileUploadService.loadAudio(audioId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("内容运营")
                .function("上传语音")
                .action("加载语音")
                .memo(audioId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(audio);
    }
}
