package com.iquanwai.confucius.biz.domain.weixin.tag;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by justin on 2017/10/23.
 */
@Service
public class TagServiceImpl implements TagService {
    @Autowired
    private RestfulHelper restfulHelper;

    @Override
    public void addTag(TagDto tagDto) {
        restfulHelper.post(ADD_TAG_SERVICE, JSONObject.toJSONString(tagDto));
    }

    @Override
    public void removeTag(TagDto tagDto) {
        restfulHelper.post(REMOVE_TAG_SERVICE, JSONObject.toJSONString(tagDto));
    }
}
