package com.iquanwai.confucius.web.pc.backend.dto;

import com.iquanwai.confucius.biz.po.fragmentation.KnowledgeDiscuss;
import lombok.Data;

/**
 * Created by 三十文
 */
@Data
public class KnowledgeDiscussDto extends KnowledgeDiscuss {

    private String headImgUrl;
    private String nickName;

    private String publishTime;

    // 是否是自己作业
    private Boolean isSelf;

}
