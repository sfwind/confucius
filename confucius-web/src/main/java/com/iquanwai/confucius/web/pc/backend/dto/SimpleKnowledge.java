package com.iquanwai.confucius.web.pc.backend.dto;

import lombok.Data;

/**
 * Created by justin on 2017/9/26.
 */
@Data
public class SimpleKnowledge {
    private int id;
    private String knowledge;

    public SimpleKnowledge(int id, String knowledge){
        this.id = id;
        this.knowledge = knowledge;
    }
}
