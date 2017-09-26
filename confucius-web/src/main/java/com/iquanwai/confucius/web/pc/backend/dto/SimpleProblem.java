package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

/**
 * Created by justin on 2017/9/20.
 */
@Data
public class SimpleProblem {
    private int id;
    private String problem;

    public SimpleProblem(int id, String problem){
        this.id = id;
        this.problem = problem;
    }
}
