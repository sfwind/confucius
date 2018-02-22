package com.iquanwai.confucius.web.pc.backend.dto;

import com.iquanwai.confucius.biz.util.page.Page;
import lombok.Data;

import java.util.List;

@Data
public class MonthlyCampPageDto {

    private List<MonthlyCampDto> monthlyCampDtoList;
    private Page page;

}
