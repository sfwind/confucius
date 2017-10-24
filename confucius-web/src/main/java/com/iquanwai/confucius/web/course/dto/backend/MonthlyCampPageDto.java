package com.iquanwai.confucius.web.course.dto.backend;

import com.iquanwai.confucius.biz.util.page.Page;
import lombok.Data;

import java.util.List;

@Data
public class MonthlyCampPageDto {

    private List<MonthlyCampDto> monthlyCampDtoList;
    private Page page;

}
