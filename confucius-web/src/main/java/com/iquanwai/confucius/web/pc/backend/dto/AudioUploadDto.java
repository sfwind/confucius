package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

/**
 * Created by 三十文 on 2017/9/22
 */
@Data
public class AudioUploadDto {
    private Integer audioId;
    private String name; // audio 中 name
    private String words; // 对应文字版
    private String ftpFileName; // 重命名添加前缀

}
