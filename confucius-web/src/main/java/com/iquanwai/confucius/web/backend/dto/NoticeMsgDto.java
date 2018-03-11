package com.iquanwai.confucius.web.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/11/11.
 */
@Data
public class NoticeMsgDto {

    private Boolean forcePush; // 是否强制推送
    private String comment; // 发送备注
    private String messageId;
    private String first;
    private String keyword1;
    private String keyword2;
    private String keyword3;
    private String keyword4;
    private String remark;
    private String remarkColor; // remark 备注颜色
    private String url; // 点击跳转 url
    private String source; //跟踪打开率的场景值
    private List<String> openids; // 发送通知名单
    private List<String> excludes; // 需要排除的人员名单

}
