package com.iquanwai.confucius.web.pc.survey.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/1/18.
 */
@Data
public class SurveyResultDto {
    /**
     * {activity=11853325,
     * name=全测试1,
     * sojumpparm=4,
     * q1=1, q2=1,2,
     * q3=填空, q4_1=姓名, q4_2=21, q4_3=test, q5=2, q6=2,3,
     * index=2,
     * timetaken=19,
     * submittime=2017-01-17 19:26:01}
     */
    private Integer activity;
    private String name;
    private Integer sojumpparm;
    private Integer index;
    private Integer timetaken;
    private String submittime;
    private Integer totalvalue;
}
