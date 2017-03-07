package com.iquanwai.confucius.web.operation;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.course.operational.PromoCodeService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.PromoCode;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.resolver.LoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/2/20.
 */
@RestController
@RequestMapping("/operation")
public class PromoCodeController {
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private PromoCodeService promoCodeService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AccountService accountService;

    private static final String PROMOTION_URL = "/operation/static/course/promotion";
    private static final String SHARE_URL = "/static/course/promotion/share?id=";

    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/promoCode/{activityCode}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> sendPromoInfo(@PathVariable String activityCode){
        new Thread(() -> {
            try {
                List<PromoCode> promoCodes = promoCodeService.getPromoCodes(activityCode);
                promoCodes.stream().forEach(promoCode -> {
                    TemplateMessage templateMessage = new TemplateMessage();
                    templateMessage.setTouser(promoCode.getOwner());
                    templateMessage.setTemplate_id(ConfigUtils.activityStartMsgKey());
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    data.put("first", new TemplateMessage.Keyword("老学员福利，错过一次，再等N年！\n" +
                            "圈圈亲设，两堂受益一生的求职&职业规划课\n"+
                            "分享优惠码，还可免费听课程！"));
                    data.put("keyword1", new TemplateMessage.Keyword("这个春天，一起来重新学习职业发展！"));
                    data.put("keyword2", new TemplateMessage.Keyword("3月1日 ~3月31日"));
                    data.put("remark", new TemplateMessage.Keyword("\n您的优惠码为:"+promoCode.getCode()+"\n点击查看活动详情", TemplateMessage.BLACK));
                    templateMessage.setData(data);
                    templateMessage.setUrl(ConfigUtils.domainName()+PROMOTION_URL);

                    templateMessageService.sendMessage(templateMessage);
                });
            } catch (Exception e) {
                LOGGER.error("发送通知失败", e);
            }
        }).start();
        return WebUtils.result("正在运行中");
    }


    @RequestMapping("/my/promoCode")
    public ResponseEntity<Map<String, Object>> getPromoInfo(LoginUser loginUser){
        Assert.notNull(loginUser, "用户不能为空");

        PromoCode promoCode = promoCodeService.getPromoCode(loginUser.getOpenId());
        PromoCodeDto promoCodeDto = new PromoCodeDto();
        promoCodeDto.setCode(promoCode.getCode());
        promoCodeDto.setId(promoCode.getId());
        Account account = accountService.getAccount(promoCode.getOwner(), false);
        if(account!=null) {
            promoCodeDto.setName(account.getNickname());
            if(StringUtils.isNotEmpty(account.getHeadimgurl())) {
                promoCodeDto.setAvatar(account.getHeadimgurl());
            }else{
                promoCodeDto.setAvatar(Profile.DEFAULT_AVATAR);
            }
        }
        promoCodeDto.setUrl(ConfigUtils.domainName()+SHARE_URL+promoCode.getId());

        OperationLog operationLog = new OperationLog()
                .module("运营活动")
                .function("求职课程推广活动")
                .action("老学员打开推荐码活动")
                .memo(loginUser.getOpenId());
        operationLogService.log(operationLog);
        return WebUtils.result(promoCodeDto);
    }


    @RequestMapping("/promoCode/{promoCodeId}")
    public ResponseEntity<Map<String, Object>> getSharePromoCode(@PathVariable Integer promoCodeId){
        PromoCode promoCode = promoCodeService.getPromoCode(promoCodeId);

        PromoCodeDto promoCodeDto = new PromoCodeDto();
        promoCodeDto.setCode(promoCode.getCode());
        Account account = accountService.getAccount(promoCode.getOwner(), false);
        if(account!=null) {
            promoCodeDto.setName(account.getNickname());
            if(StringUtils.isNotEmpty(account.getHeadimgurl())) {
                promoCodeDto.setAvatar(account.getHeadimgurl());
            }else{
                promoCodeDto.setAvatar(Profile.DEFAULT_AVATAR);
            }
        }
        promoCodeDto.setUrl(ConfigUtils.domainName()+SHARE_URL+promoCode.getId());
        OperationLog operationLog = new OperationLog()
                .module("运营活动")
                .function("求职课程推广活动")
                .action("新人打开推荐码活动")
                .memo(promoCodeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(promoCodeDto);
    }

}
