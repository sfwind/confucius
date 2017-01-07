package com.iquanwai.confucius.web.account.dto;

import com.iquanwai.confucius.web.pc.dto.ProblemDto;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2016/12/28.
 */
@Data
public class FragmentDto {
    private List<ProblemDto> problemList;
}
