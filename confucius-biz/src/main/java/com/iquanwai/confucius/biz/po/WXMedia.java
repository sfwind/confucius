package com.iquanwai.confucius.biz.po;

import lombok.Data;

/**
 * 微信素材
 */
@Data
public class WXMedia {
    private String mediaId;//meida_id
    private String url;//永久素材url
    private String remark;//素材信息
    private Integer del;
}
