package com.iquanwai.confucius.web;

import com.iquanwai.confucius.biz.domain.course.signup.RiseMemberManager;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.web.resolver.LoginUser;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RequestMapping(value = "/redirect")
@Controller
public class RedirectController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private RiseMemberManager riseMemberManager;

    /**
     * 小课洞见文章跳转区分
     * @param problemId 小课id
     */
    @RequestMapping(value = "/kit_package/{id}")
    public void redirectKitPackage(LoginUser loginUser, @PathVariable(value = "id") Integer problemId, HttpServletResponse response) {
        OperationLog operationLog = new OperationLog()
                .openid(loginUser.getOpenId())
                .module("扫码")
                .function("小课洞见")
                .action("redirect")
                .memo("" + problemId);
        operationLogService.log(operationLog);
        List<RiseMember> members = riseMemberManager.businessSchoolMember(loginUser.getId());
        if (CollectionUtils.isNotEmpty(members)) {
            // 商学院、专业版
            try {
                response.sendRedirect("/rise/static/plan/view?id=" + problemId + "&show=true");
            } catch (IOException e) {
                logger.error("重定向失败,{}", loginUser.getOpenId());
            }
        } else {
            // 非商学院、专业版
            try {
                response.sendRedirect("https://shimo.im/docs/zKmdQvsegVcWqlgt");
            } catch (IOException e) {
                logger.error("重定向失败,{}", loginUser.getOpenId());
            }
        }
    }
}
