package com.iquanwai.confucius.web.weixin;

import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.util.ThreadPool;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * Created by justin on 16/9/27.
 */
@RequestMapping("/wx/user")
@Controller
public class FollowController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountService accountService;

    @RequestMapping("/ip")
    public ResponseEntity<Map<String, Object>> getIp(HttpServletRequest request, LoginUser loginUser) {
        String remoteIp = request.getHeader("X-Forwarded-For");
        LOGGER.info("用户:{},ip:{}", loginUser == null ? null : loginUser.getOpenId(), remoteIp);
        return WebUtils.result(remoteIp);
    }

}
