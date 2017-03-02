package com.iquanwai.confucius.web.performance.dto;

import com.iquanwai.confucius.biz.po.performance.PagePerformance;
import lombok.Data;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Created by yongqiang.shen on 2017/3/2.
 */
@Data
public class PerformanceSourceInfoDto {
    private String app;
    private String url;
    private String sr;
    private String vp;
    private Integer csz;
    private String uuid;
    private String data;

    public PagePerformance toPo() {
        PagePerformance po;
        ObjectMapper mapper = new ObjectMapper();
        try {
            PagePerformanceDto pageDto = mapper.readValue(this.data, PagePerformanceDto.class);
            po = pageDto.getPage();
            po.setApp(this.app);
            po.setUrl(this.url);
            po.setScreen(this.sr);
            po.setViewport(this.vp);
            po.setCookieSize(this.csz);
            po.setUuid(this.uuid);
            return po;
        } catch (Exception e) {
            return new PagePerformance();
        }
    }
}
