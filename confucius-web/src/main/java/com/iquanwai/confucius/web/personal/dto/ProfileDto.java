package com.iquanwai.confucius.web.personal.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by nethunder on 2017/2/4.
 */
@Data
public class ProfileDto {
    private String industry; //行业
    private String function; //职业
    private String workingLife; //工作年限
    private String city; //城市
    private String province; //省份
    private Boolean isFull;

    public void isFullCheck() {
        this.isFull = StringUtils.isNoneEmpty(industry, function, workingLife, city, province);
    }
}
