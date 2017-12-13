package com.iquanwai.confucius.web.recommend;

import com.iquanwai.confucius.biz.domain.fragmentation.recommedation.RecommedationService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/recommend")
public class RecommendationController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private RecommedationService recommedationService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获得当前用户的riseId
     *
     * @param loginUser
     * @return
     */
    @RequestMapping("/get/rise")
    public ResponseEntity<Map<String, Object>> getRise(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        Profile profile = accountService.getProfile(loginUser.getOpenId());
        logger.info("进入获得当前用户");

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("用户推荐")
                .function("获得当前用户")
                .action("获得当前用户riseId");
        operationLogService.log(operationLog);

        return WebUtils.result(profile.getRiseId());
    }

    @RequestMapping("/add/user/{riseId}")
    public ResponseEntity<Map<String, Object>> addUserRecommendation(LoginUser loginUser, @PathVariable String riseId) {
        Assert.notNull(loginUser, "用户不能为空");
        String openId = loginUser.getOpenId();

        OperationLog operationLog = OperationLog.create().openid(openId)
                .module("用户推荐")
                .function("添加用户推荐")
                .action("添加用户推荐信息");
        operationLogService.log(operationLog);

        return WebUtils.result(recommedationService.addUserRecommedation(openId, riseId));
    }
}
