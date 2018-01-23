package com.iquanwai.confucius.biz.domain.message;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.common.customer.ShortMessageSubmitDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.domain.weixin.message.template.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.template.TemplateMessageService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.message.ShortMessageSubmit;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
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
    private RedisUtil redisUtil;
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ShortMessageSubmitDao shortMessageSubmitDao;
    @Autowired
    private TemplateMessageService templateMessageService;

    private static final String MIN_SEND_KEY = "short:min:{key}";
    private static final String HOUR_SEND_KEY = "short:hour:{key}";
    private static final String DAY_SEND_KEY = "short:day:{key}";

    @Override
    public Pair<Integer, String> checkSendAble(ShortMessage shortMessage) {
        // 1.profileId查询
        Profile profile = accountService.getProfile(shortMessage.getProfileId());
        if (profile == null) {
            // profileId异常
            return new MutablePair<>(-201, "发送失败，请联系管理员");
        }

        // 2.发送条数限制
        SendLimit limit = getUserSendLimit(shortMessage.getProfileId());
        if (limit.getMinSend() >= ConfigUtils.getMinSendLimit()) {
            return new MutablePair<>(-1, "操作过于频繁，请稍后再试");
        }
        if (limit.getHourSend() >= ConfigUtils.getHourSendLimit()) {
            return new MutablePair<>(-2, "操作过于频繁，请稍后再试");
        }
        if (limit.getDaySend() >= ConfigUtils.getDaySendLimit()) {
            return new MutablePair<>(-3, "操作过于频繁，请稍后再试");
        }

//        // 3.电话号码数量检查
//        if (shortMessage.getPhones() == null || shortMessage.getPhones().size() > MAX_PHONE_COUNT) {
//            return new MutablePair<>(-202, MAX_PHONE_COUNT);
//        }
        // 4.文字内容检查
        String content = shortMessage.getContent();
        if (content.length() > MAX_CONTENT_SIZE) {
            return new MutablePair<>(-203, "短信内容过长，请进行精简");
        }
        // 通过检查
        return new MutablePair<>(1, "ok");
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
                    this.smsAlarm(openid,
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
        addSendCount(profileId);
    }

    @Override
    public void smsAlarm(String openId, String nickname, String msgId, String result, String desc) {
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



    private SendLimit getUserSendLimit(Integer profileId){
        if (profileId == null) {
            return null;
        }
        Integer minSend = redisUtil.getInt(MIN_SEND_KEY.replace("{key}", profileId.toString()), 0);
        Integer hourSend = redisUtil.getInt(HOUR_SEND_KEY.replace("{key}", profileId.toString()), 0);
        Integer daySend = redisUtil.getInt(DAY_SEND_KEY.replace("{key}", profileId.toString()), 0);

        SendLimit limit = new SendLimit();
        limit.setProfileId(profileId);
        limit.setMinSend(minSend);
        limit.setHourSend(hourSend);
        limit.setDaySend(daySend);
        return limit;
    }


    private void addSendCount(Integer profileId) {
        SendLimit userSendLimit = getUserSendLimit(profileId);
        String minKey = MIN_SEND_KEY.replace("{key}", profileId.toString());
        String hourKey = HOUR_SEND_KEY.replace("{key}", profileId.toString());
        String dayKey = DAY_SEND_KEY.replace("{key}", profileId.toString());
        Long minExpired;
        Long hourExpired;
        Long dayExpired;
        if (userSendLimit.getMinSend() == 0) {
            minExpired = 60L;
        } else {
            minExpired = redisUtil.getRemainTime(minKey);
        }
        if (userSendLimit.getHourSend() == 0) {
            hourExpired = 60 * 60L;
        } else {
            hourExpired = redisUtil.getRemainTime(hourKey);
        }
        if (userSendLimit.getDaySend() == 0) {
            dayExpired = DateUtils.nextDayRemainSeconds(new Date());
        } else {
            dayExpired = redisUtil.getRemainTime(dayKey);
        }

        redisUtil.set(minKey, userSendLimit.getMinSend() + 1, minExpired);
        redisUtil.set(hourKey, userSendLimit.getHourSend() + 1, hourExpired);
        redisUtil.set(dayKey, userSendLimit.getDaySend() + 1, dayExpired);
    }

}
