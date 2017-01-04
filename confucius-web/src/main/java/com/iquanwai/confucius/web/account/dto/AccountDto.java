package com.iquanwai.confucius.web.account.dto;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.resolver.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <pre>
 * Created by nethunder on 2016/12/25.
 * user:{ // 用户数据
 *  role:// 角色,目前只有student，stranger
 *  weixin:{ // 微信数据
 *    openid,
 *    weixinName,
 *    headimgUrl
 *  },
 *  course:{ // 课程数据
 *   confucius:{ // 体系化课程
 *   },
 *   fragment:{ // 碎片化课程
 *    problemList:[{
 *      id,// 问题id
 *      status,//问题状态（0-待解决，1-解决中，2-已解决）
 *      problem,// 工作生活中遇到的问题
 *      pic,// 头图链接
 *      challengeList:[{// 这里刚开始只放解决中的问题
 *        id, // 挑战任务id
 *        description, // "图文混排内容", //html
 *        pic, // "http://someurl",  //图片url
 *        problemId,//问题id
 *        pcurl,"http://someurl", //pc端url
 *        submitted, true, //是否提交过
 *        content, "balbal" //提交内容
 *      }]
 *    ]
 *   }
 * }
 * </pre>
 **/
@Data
public class AccountDto {
    private String openId;
    private LoginUser weixin;
    private String role;
    private CourseDto course;
}
