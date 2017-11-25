package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class CampRiseCertificateDao {

    private Integer type;
    private List<String> memberIds;

}
