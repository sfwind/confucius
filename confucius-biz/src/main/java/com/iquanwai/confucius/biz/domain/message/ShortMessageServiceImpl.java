package com.iquanwai.confucius.biz.domain.message;

import com.alibaba.fastjson.JSONObject;
import com.iquanwai.confucius.biz.dao.common.customer.ShortMessageRedisDao;
import com.iquanwai.confucius.biz.dao.common.customer.ShortMessageSubmitDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.message.ShortMessageSubmit;
import com.iquanwai.confucius.biz.util.CommonUtils;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        // 3.电话号码数量检查
        if (shortMessage.getPhones() == null || shortMessage.getPhones().size() > MAX_PHONE_COUNT) {
            return new MutablePair<>(-202, MAX_PHONE_COUNT);
        }
        // 4.文字内容检查
        String content = CommonUtils.placeholderReplace(shortMessage.getContent(), shortMessage.getReplace());
        if (content.length() > MAX_CONTENT_SIZE) {
            return new MutablePair<>(-203, MAX_CONTENT_SIZE);
        }
        // 通过检查
        return new MutablePair<>(1, 0);
    }


    @Override
    public SMSSendResult sendMessage(ShortMessage shortMessage) {
        // 初始化请求参数
        SMSConfig config = ConfigUtils.getBizMsgConfig();
        String content = CommonUtils.placeholderReplace(shortMessage.getContent(), shortMessage.getReplace());
        String phones = StringUtils.join(shortMessage.getPhones().iterator(), ",");
        ShortMessageSubmit shortMessageSubmit = new ShortMessageSubmit();
        shortMessageSubmit.setAccount(config.getAccount());
        shortMessageSubmit.setPassword(config.getPassword());
        shortMessageSubmit.setSign(config.getSign());
        shortMessageSubmit.setContent(content);
        shortMessageSubmit.setPhones(phones);
        shortMessageSubmit.setMsgId(CommonUtils.randomString(32));
        String json = JSONObject.toJSONString(shortMessageSubmit);
        logger.info("param:{}", json);
        // 开始请求
        String post = restfulHelper.post(SMS_SEND_URL, json);
        // 解析请求结果
        SMSSendResult smsSendResult = JSONObject.parseObject(post, SMSSendResult.class);
        logger.info("result:{}", smsSendResult);
        if (smsSendResult != null) {
            shortMessageSubmit.setResult(smsSendResult.getResult());
            shortMessageSubmit.setDescription(smsSendResult.getDesc());
            shortMessageSubmit.setFailPhones(smsSendResult.getFailPhones());
        }
        shortMessageSubmitDao.insert(shortMessageSubmit);
        return smsSendResult;
    }

    @Override
    public void raiseSendCount(Integer profileId){
        shortMessageRedisDao.addSendCount(profileId);
    }

}
