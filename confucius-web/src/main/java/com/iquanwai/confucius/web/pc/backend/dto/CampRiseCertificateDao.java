package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class CampRiseCertificateDao {

    private Integer type;
    private Integer year;
    private Integer month;
    private List<String> memberIds;

}
