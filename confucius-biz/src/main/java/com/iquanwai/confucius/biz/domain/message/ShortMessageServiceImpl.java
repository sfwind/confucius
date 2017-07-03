package com.iquanwai.confucius.biz.domain.message;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.common.customer.ShortMessageRedisDao;
import com.iquanwai.confucius.biz.dao.common.customer.ShortMessageSubmitDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.message.ShortMessageSubmit;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2017/6/15.
 */
@Service
public class ShortMessageServiceImpl implements ShortMessageService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private ShortMessageRedisDao shortMessageRedisDao;
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ShortMessageSubmitDao shortMessageSubmitDao;
    @Autowired
    private TemplateMessageService templateMessageService;

    @Override
    public Pair<Integer, Integer> checkSendAble(ShortMessage shortMessage) {
        // 1.profileId查询
        Profile profile = accountService.getProfile(shortMessage.getProfileId());
        if (profile == null) {
            // profileId异常
            return new MutablePair<>(-201, 0);
        }

        // 2.发送条数限制
        SendLimit limit = shortMessageRedisDao.getUserSendLimit(shortMessage.getProfileId());
        if (limit.getMinSend() >= ConfigUtils.getMinSendLimit()) {
            return new MutablePair<>(-1, limit.getMinSend());
        }
        if (limit.getHourSend() >= ConfigUtils.getHourSendLimit()) {
            return new MutablePair<>(-2, limit.getHourSend());
        }
        if (limit.getDaySend() >= ConfigUtils.getDaySendLimit()) {
            return new MutablePair<>(-3, limit.getDaySend());
        }
//        // 3.电话号码数量检查
//        if (shortMessage.getPhones() == null || shortMessage.getPhones().size() > MAX_PHONE_COUNT) {
//            return new MutablePair<>(-202, MAX_PHONE_COUNT);
//        }
        // 4.文字内容检查
        String content = shortMessage.getContent();
        if (content.length() > MAX_CONTENT_SIZE) {
            return new MutablePair<>(-203, MAX_CONTENT_SIZE);
        }
        // 通过检查
        return new MutablePair<>(1, 0);
    }


    @Override
    public SMSSendResult sendMessage(ShortMessage shortMessage) {
        // 初始化请求参数
        SMSConfig config = null;
        if (ShortMessage.MARKETING.equals(shortMessage.getType())) {
            // 营销短信
            config = ConfigUtils.getMarketMsgConfig();
        } else {
            // 默认行业短信
            config = ConfigUtils.getBizMsgConfig();
            // 如果是null或者其他乱七八糟的type或者本身即是BUSINESS，默认设置为BUSINESS
            shortMessage.setType(ShortMessage.BUSINESS);
        }

        String content = shortMessage.getContent();
        String phone = shortMessage.getPhone();
        ShortMessageSubmit shortMessageSubmit = new ShortMessageSubmit();
        shortMessageSubmit.setAccount(config.getAccount());
        shortMessageSubmit.setPassword(config.getPassword());
        shortMessageSubmit.setSign(config.getSign());
        shortMessageSubmit.setContent(content);
        shortMessageSubmit.setPhones(phone);
        shortMessageSubmit.setMsgId(CommonUtils.randomString(32));
        String json = JSONObject.toJSONString(shortMessageSubmit);
        logger.info("param:{}", json);
        // 开始请求
        String post = restfulHelper.post(SMS_SEND_URL, json);
        // 解析请求结果
        SMSSendResult smsSendResult = JSONObject.parseObject(post, SMSSendResult.class);
        logger.info("result:{}", smsSendResult);
        // 填充请求结果
        if (smsSendResult != null) {
            shortMessageSubmit.setResult(smsSendResult.getResult());
            shortMessageSubmit.setDescription(smsSendResult.getDesc());
            shortMessageSubmit.setFailPhones(smsSendResult.getFailPhones());
        }
        shortMessageSubmit.setProfileId(shortMessage.getProfileId());
        if (shortMessageSubmit.getSendTime() == null) {
            shortMessageSubmit.setSendTime(new Date());
        }
        // 设置短信类型
        shortMessageSubmit.setType(shortMessage.getType());
        shortMessageSubmitDao.insert(shortMessageSubmit);

        if (smsSendResult == null || !"0".equals(smsSendResult.getResult())) {
            if (smsSendResult == null ||
                    // 短信内容超过最大限制
                    (!"6".equals(smsSendResult.getResult()) &&
                            // 定时发送时间格式错误
                            !"8".equals(smsSendResult.getResult()) &&
                            // 手机号码为空
                            !"14".equals(smsSendResult.getResult()) &&
                            // 短信内容为空
                            !"21".equals(smsSendResult.getResult()))) {
                // 其他情况发送报警消息
                List<String> alarmList = ConfigUtils.getAlarmList();
                alarmList.forEach(openid -> {
                    this.SMSAlarm(openid,
                            shortMessage.getNickname(),
                            shortMessageSubmit.getMsgId(),
                            smsSendResult != null ? smsSendResult.getResult() : "空",
                            smsSendResult != null ? smsSendResult.getDesc() : "空");
                });
            }
        }
        return smsSendResult;
    }

    @Override
    public void raiseSendCount(Integer profileId){
        shortMessageRedisDao.addSendCount(profileId);
    }

    @Override
    public void SMSAlarm(String openId, String nickname, String msgId, String result, String desc) {
        String key = ConfigUtils.incompleteTaskMsgKey();
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(openId);

        templateMessage.setTemplate_id(key);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        String message = "发送人：" + (nickname == null ? "无昵称" : nickname) + "\n" +
                "msgId：" + msgId + "\n" +
                "异常码：" + result + "\n" +
                "错误信息：" + desc;

        data.put("first", new TemplateMessage.Keyword("报警! 短信发送失败!\n"));
        data.put("keyword1", new TemplateMessage.Keyword("处理短信发送异常"));
        data.put("keyword2", new TemplateMessage.Keyword("高"));
        data.put("remark", new TemplateMessage.Keyword(message));
        templateMessageService.sendMessage(templateMessage);
    }

}
