package com.iquanwai.confucius.biz.domain.weixin.file;

public interface WXMediaService {

    /**
     * 插入Media表
     * @param mediaId
     * @param url
     * @param remark
     * @return
     */
     Integer insertMediaId(String mediaId,String url,String remark);
}
