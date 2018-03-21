package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by justin on 2017/9/20.
 */
@Data
@AllArgsConstructor
public class SimpleProblem {
    private int id;
    private String problem;
    private String abbreviation;
}
