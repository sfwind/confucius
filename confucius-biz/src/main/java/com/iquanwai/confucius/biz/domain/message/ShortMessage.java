package com.iquanwai.confucius.biz.domain.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Integer type; // 短信类型 1.非营销  2.营销


    public static final Integer MARKETING = 2;
    public static final Integer BUSINESS = 1;

}
