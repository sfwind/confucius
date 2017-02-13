package com.iquanwai.confucius.web.customer.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/2/8.
 */
@Data
public class CourseDto {
    private Integer id;
    private String name;
    private Boolean hasCertificateNo;
    private Boolean hasRealName;
    private Boolean noCertificate;
}
