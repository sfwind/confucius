package com.iquanwai.confucius.biz.po.fragmentation;

import com.iquanwai.confucius.biz.domain.fragmentation.plan.Practice;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class ImprovementPlan {
    private int id;
    private String openid; //openid
    private Integer problemId; //问题id
    private Date startDate; //开始日期
    private Date endDate; //结束日期
    private Date closeDate; //课程关闭时间
    private Integer status; //执行状态（1-正在进行, 2-已结束, 3-已过期）
    private Integer point; //积分
    private Integer complete; //完成训练个数
    private Integer total; //任务总数
    private Integer keycnt; //钥匙数量
    private Problem problem; //非db字段 问题
    private List<Practice> practice; //非db字段
    private Integer length; //非db字段 总时长
    private Integer deadline; //非db字段 离截止日期天数
}
