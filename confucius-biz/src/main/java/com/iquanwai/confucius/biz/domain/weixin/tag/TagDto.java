package com.iquanwai.confucius.biz.domain.weixin.tag;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 2017/10/23.
 */
@Data
public class TagDto {
    private List<String> openid_list;
    private int tagid;
}
