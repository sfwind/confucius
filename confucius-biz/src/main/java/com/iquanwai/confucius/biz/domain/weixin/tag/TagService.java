package com.iquanwai.confucius.biz.domain.weixin.tag;

/**
 * Created by justin on 2017/10/23.
 */
public interface TagService {
    //批量为用户打标签
    String ADD_TAG_SERVICE = "https://api.weixin.qq.com/cgi-bin/tags/members/batchtagging?access_token={access_token}";
    //批量为用户取消标签
    String REMOVE_TAG_SERVICE = "https://api.weixin.qq.com/cgi-bin/tags/members/batchuntagging?access_token={access_token}";

    void addTag(TagDto tagDto);

    void removeTag(TagDto tagDto);
}
