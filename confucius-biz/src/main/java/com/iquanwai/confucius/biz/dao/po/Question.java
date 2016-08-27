package com.iquanwai.confucius.biz.dao.po;

import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.List;

/**
 * Created by justin on 16/8/25.
 */
@Data
@Alias("question")
public class Question {
    private int id;
    private Integer materialId; //素材id
    private String subject; //题干
    private String analysis; //题目分析
    private Integer point; //分值
    private List<Choice> choiceList;
}
