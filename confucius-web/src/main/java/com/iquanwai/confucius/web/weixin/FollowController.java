package com.iquanwai.confucius.web.weixin;

import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.Map;

/**
 * Created by justin on 16/9/27.
 */
@RequestMapping("/wx/user")
@Controller
public class FollowController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountService accountService;

    @RequestMapping("/all")
    public ResponseEntity<Map<String, Object>> getAll() throws IOException {
        try {
            accountService.collectUsers();
            return WebUtils.success();
        }catch (Exception e){
            LOGGER.error("get all followers failed", e);
        }
        return WebUtils.error("get all followers failed");
    }

}