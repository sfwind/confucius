package com.iquanwai.confucius.web.weixin;

import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.util.ThreadPool;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/wx/user")
public class WXUserController {

    @Autowired
    private AccountService accountService;


    @RequestMapping(value = "/update/image",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> updateWXImg(@RequestParam("memberId")String memberId){
        ThreadPool.execute(()->{
            Profile profile = accountService.loadProfileByMemberId(memberId);
            if(profile!=null){
                //更新用户头像
                accountService.updateProfileByWeiXin(profile);
                //刷新mq
            }
        });
        return WebUtils.success();
    }


}
