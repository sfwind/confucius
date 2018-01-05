package com.iquanwai.confucius.biz.po;

import lombok.Data;

@Data
public class CodeRotate {

    private Integer id;
    private String sceneCode; // 场景号
    private Integer sequence; // 顺序
    private String codeUrl; // 二维码链接
    private String mediaId; // 微信素材 media_id
    private Boolean del; // 是否删除

}
