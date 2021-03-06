package com.iquanwai.confucius.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/12/26.
 */
@Data
public class WhiteList {
    private int id;
    private String function;
    private Integer profileId;

    //碎片化练习
    public final static String FRAG_PRACTICE = "FRAG_PRACTICE";
    public final static String PAY_TEST = "PAY_TEST";
    public final static String TEST = "TEST";
    public final static String INTERVIEWER = "INTERVIEWER";
}

