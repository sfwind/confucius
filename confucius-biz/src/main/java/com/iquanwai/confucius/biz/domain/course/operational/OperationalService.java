package com.iquanwai.confucius.biz.domain.course.operational;

/**
 * Created by justin on 16/10/8.
 */
public interface OperationalService {
    /**
     * 为班级成员分配天使
     * */
    boolean angelAssign(Integer classId);

    /**
     * 为所有明天开班的班级成员分配天使
     * */
    void angelAssign();

}
