package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.po.RichText;

/**
 * Created by 三十文
 */
public interface FlowDataService {
    RichText insertRichText(String title, String content);
}
