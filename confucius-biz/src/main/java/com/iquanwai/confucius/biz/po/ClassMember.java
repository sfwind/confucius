package com.iquanwai.confucius.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/8/29.
 */
@Data
public class ClassMember {
    private int id;
    private String memberId; // 学号
    private String openId;  //openid
    private Integer classId; //班级id
    private Boolean graduate; //是否毕业（0-否，1-是）
    private Integer score;    //课程积分
    private Integer level;  //课程等级，和勋章挂钩
    private Boolean superb; //是否优秀学员（1-是，0-否)
    private String progress; //学员进度，章节的sequence集合，用逗号隔开
    private String complete; //学员完成进度，章节的sequence集合，用逗号隔开
    private Boolean pass;  //是否通过课程（0-否，1-是）
    private Integer classProgress; //课程进度，章节的id
    private Integer courseId; //课程id
    private String certificateNo; // 毕业证书编号

}
