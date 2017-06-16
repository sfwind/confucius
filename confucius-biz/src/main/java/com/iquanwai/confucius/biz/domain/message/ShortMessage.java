package com.iquanwai.confucius.biz.domain.message;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2017/6/15.
 */
@Data
public class ShortMessage {
    private Integer profileId;
    private List<String> phones;
    private String content;
    private Map<String,String> replace;
}
