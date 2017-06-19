package com.iquanwai.confucius.biz.domain.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by nethunder on 2017/6/15.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShortMessage {
    private Integer profileId;// 进行发送操作的profileId
    private String nickname; // 进行发送操作的nickname
    private String phone;
    private String content;
    private Map<String,String> replace;

}
