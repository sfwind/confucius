package com.iquanwai.confucius.biz.util;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.message.SMSConfig;
import com.iquanwai.confucius.biz.util.zk.ZKConfigUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ConfigUtils {
    private static Config config;
    private static Config localconfig;
    private static Config fileconfig;
    private static ZKConfigUtils zkConfigUtils;

    private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    static {
        loadLocalConfig();
        zkConfigUtils = new ZKConfigUtils();
    }

    private static void loadLocalConfig() {
        logger.info("load local config");
        config = ConfigFactory.load("localconfig");
        fileconfig = ConfigFactory.parseFile(new File("/data/config/localconfig"));
        config = fileconfig.withFallback(config);
    }

    public static String getToken() {
        return getValue("token");
    }

    public static String getAppid() {
        return getValue("appid");
    }

    public static boolean logSwitch() {
        return getBooleanValue("open.log");
    }

    public static String getAPIKey() {
        return getValue("api.key");
    }

    public static String getSecret() {
        return getValue("secret");
    }

    public static int getJsSignatureInterval() {
        return getIntValue("js.internal");
    }

    public static boolean isDebug() {
        return getBooleanValue("debug");
    }

    public static boolean isFrontDebug() {
        return getBooleanValue("front.debug");
    }

    public static boolean logDetail() {
        return getBooleanValue("log.debug");
    }

    public static String getMch_id() {
        return getValue("mch_id");
    }

    public static String getExternalIP() {
        return getValue("external.ip");
    }

    public static Integer getBillOpenMinute() {
        return getIntValue("bill.open.minute");
    }

    public static String incompleteTaskMsgKey() {
        return getValue("incomplete.task.msg");
    }

    public static String accountChangeMsgKey() {
        return getValue("account.change.message");
    }

    public static String coursePassMsgKey() {
        return getValue("course.pass.msg");
    }

    public static String domainName() {
        return getValue("app.domain");
    }

    public static String adapterDomainName() {
        return getValue("adapter.domain");
    }

    public static String resourceDomainName() {
        return getValue("resource.domain");
    }

    public static String realDomainName() {
        return getValue("app.domainname");
    }

    public static String getInterviewers() {
        return getValue("interviewers");
    }

    public static String staticResourceUrl(String domainName) {
        String url = getValue("static.resource.url");
        //测试环境防浏览器缓存，添加随机参数
        if (url.endsWith("?")) {
            url = url.concat("_t=").concat(new Random().nextInt() + "");
        }

        if (!StringUtils.isEmpty(domainName)) {
            url = replaceDomainName(url, domainName);
        }

        return url;
    }

    public static String staticPayUrl(String domainName) {
        String url = getValue("static.pay.resource.url");
        //测试环境防浏览器缓存，添加随机参数
        if (url.endsWith("?")) {
            url = url.concat("_t=").concat(new Random().nextInt() + "");
        }

        if (!StringUtils.isEmpty(domainName)) {
            url = replaceDomainName(url, domainName);
        }

        return url;
    }

    public static String staticPcResourceUrl(String domainName) {
        String url = getValue("static.pc.resource.url");
        //测试环境防浏览器缓存，添加随机参数
        if (url.endsWith("?")) {
            url = url.concat("_t=").concat(new Random().nextInt() + "");
        }

        if (!StringUtils.isEmpty(domainName)) {
            url = replaceDomainName(url, domainName);
        }

        return url;
    }

    public static String gaId() {
        return getValue("ga.id");
    }

    public static String getDefaultOpenid() {
        return getValue("default.openid");
    }

    public static String getDefaultWeMiniOpenId() {
        return getValue("default.weminiopenid");
    }

    public static String getDefaultUnionId() {
        return getValue("default.unionid");
    }

    public static Integer getDefaultProfileId() {
        return getIntValue("default.profile.id");
    }

    /**
     * 业务配置获取
     */
    public static String getValue(String key) {
        String value;
        if (config.hasPath(key)) {
            value = config.getString(key);
        } else {
            value = zkConfigUtils.getValue(key);
            if (value == null) {
                value = zkConfigUtils.getArchValue(key);
            }
        }
        if (value != null) {
            // 去掉回车，换行，tab键
            value = value.replaceAll("\r|\n|\t", "");
        }
        return value;
    }

    public static Boolean getBooleanValue(String key) {
        if (config.hasPath(key)) {
            return config.getBoolean(key);
        } else {
            Boolean value = zkConfigUtils.getBooleanValue(key);
            if (value == null) {
                value = zkConfigUtils.getBooleanValue(key);
            }
            return value;
        }
    }

    public static Integer getIntValue(String key) {
        if (config.hasPath(key)) {
            return config.getInt(key);
        } else {
            Integer value = zkConfigUtils.getIntValue(key);
            if (value == null) {
                value = zkConfigUtils.getIntValue(key);
            }
            return value;
        }
    }

    public static Double getDoubleValue(String key) {
        if (config.hasPath(key)) {
            return config.getDouble(key);
        } else {
            Double value = zkConfigUtils.getDoubleValue(key);
            if (value == null) {
                value = zkConfigUtils.getDoubleValue(key);
            }
            return value;
        }
    }

    public static List<Integer> getWorkScoreList() {
        String scores = getValue("work.difficulty.score");
        String[] split = scores.split(",");
        List<Integer> list = Lists.newArrayList();
        for (String score : split) {
            list.add(Integer.valueOf(score));
        }
        return list;
    }

    public static Integer getChallengeScore() {
        return getIntValue("challenge.score");
    }

    public static Boolean isDevelopment() {
        return getBooleanValue("development");
    }

    public static Integer getFeedBackId() {
        return getIntValue("wjx.feedback");
    }

    public static String willCloseMsgKey() {
        return getValue("will.close.task.msg");
    }

    public static String getUploadDomain() {
        return getValue("upload.image.domain");
    }

    public static String getPicturePrefix() {
        return getValue("qiniu.picture.prefix");
    }

    public static Date getRisePayStopTime() {
        return DateUtils.parseStringToDateTime(getValue("rise.member.pay.stop.time"));
    }

    public static String getIntegratedPracticeIndex() {
        return getValue("integrated.practice.index");
    }

    public static String getPcAppId() {
        return getValue("rise.web.appid");
    }

    public static String getPcSecret() {
        return getValue("rise.web.secret");
    }

    public static Integer getMinSendLimit() {
        return getIntValue("sms.min.send.limit");
    }

    public static Integer getHourSendLimit() {
        return getIntValue("sms.hour.send.limit");
    }

    public static Integer getDaySendLimit() {
        return getIntValue("sms.day.send.limit");
    }

    public static String getBizAccount() {
        return getValue("sms.business.account");
    }

    public static String getBizPassword() {
        return getValue("sms.business.password");
    }

    public static String getSMSSign() {
        return getValue("sms.sign");
    }

    public static String getMarketAccount() {
        return getValue("sms.market.account");
    }

    public static String getMarketPassword() {
        return getValue("sms.market.password");
    }

    public static SMSConfig getBizMsgConfig() {
        SMSConfig smsConfig = new SMSConfig();
        smsConfig.setAccount(getBizAccount());
        smsConfig.setPassword(getBizPassword());
        smsConfig.setSign(getSMSSign());
        return smsConfig;
    }

    public static SMSConfig getMarketMsgConfig() {
        SMSConfig smsConfig = new SMSConfig();
        smsConfig.setAccount(getMarketAccount());
        smsConfig.setPassword(getMarketPassword());
        smsConfig.setSign(getSMSSign());
        return smsConfig;
    }

    public static List<String> getAlarmList() {
        List<String> list = Lists.newArrayList();
        String[] split = getValue("sms.alarm.openids").split(",");
        CollectionUtils.addAll(list, split);
        return list;
    }

    public static Integer getVoteScore() {
        return getIntValue("vote.score");
    }

    public static String getFtpUser() {
        return getValue("ftp.username");
    }

    public static String getFtpPassword() {
        return getValue("ftp.password");
    }

    public static String getFtpHost() {
        return getValue("ftp.host");
    }

    public static Boolean reducePriceForNotElite() {
        return getBooleanValue("business.school.price.reduce.not.elite");
    }

    public static List<String> getDevelopOpenIds() {
        String openIdsStr = getValue("sms.alarm.openids");
        return Lists.newArrayList(openIdsStr.split(","));
    }

    public static String replaceDomainName(String url, String domainName) {
        String urlPattern = "^((http://)|(https://))?([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}(/)";
        //替换
        return url.replaceAll(urlPattern, "http://" + domainName + "/");
    }

    public static String getCampPayInfo() {
        return getValue("camp.pay.json.info");
    }

    public static String getWeMiniAppId() {
        return getValue("wemini.appid");
    }

    public static String getWeMiniAppSecret() {
        return getValue("wemini.secrect");
    }

    public static String getAlipayNotifyDomain() {
        return getValue("ali.pay.notify.domain");
    }

    public static String getAlipayAppId() {
        return getValue("alipay.appid");
    }

    public static String getAlipayPrivateKey() {
        return getValue("alipay.private.key");
    }

    public static String getAlipayPublicKey() {
        return getValue("alipay.public.key");
    }

    public static String getAlipayGateway() {
        return getValue("alipay.gateway");
    }

    public static Integer getLearningYear() {
        return getIntValue("learning.year");
    }

    public static Integer getLearningMonth() {
        return getIntValue("learning.month");
    }

    public static String getSensorsProject() {
        return getValue("sensors.project");
    }

    public static Boolean getPayApplyFlag() {
        return getBooleanValue("pay.apply.flag");
    }

    public static String getCoreApplyQrCode() {
        return getValue("core.apply.qr.code");
    }

    public static String getBusinessThoughtApplyQrCode() {
        return getValue("thought.apply.qr.code");
    }

    public static String getHeadTeacherWeiWeiMediaId() {
        return getValue("pay.success.thought.reply.image");
    }
}
