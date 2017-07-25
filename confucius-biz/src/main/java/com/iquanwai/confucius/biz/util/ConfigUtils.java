package com.iquanwai.confucius.biz.util;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.message.SMSConfig;
import com.iquanwai.confucius.biz.util.zk.ZKConfigUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ConfigUtils {
	private static Config config;
	private static Config localconfig;
	private static Config fileconfig;
	private static ZKConfigUtils zkConfigUtils;

	private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);


	private static Timer timer;
	static{
		loadConfig();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				loadConfig();
			}
		}, 0, 1000*60);
		zkConfigUtils = new ZKConfigUtils();
	}

	private static void loadConfig() {
		config = ConfigFactory.load("localconfig");
		fileconfig = ConfigFactory.parseFile(new File("/data/config/localconfig"));
		config = fileconfig.withFallback(config);
	}

	public static Boolean payPrePublish(){
		return getBooleanValue("pay.pre.publish");
	}

	public static String getToken(){
		return getValue("token");
	}

	public static String getEncodingAESKey(){
		return getValue("encoding.aes.key");
	}

	public static String getAppid() {
		return getValue("appid");
	}

	public static boolean logSwitch() {
		return getBooleanValue("open.log");
	}

	public static boolean pressTestSwitch(){
		return getBooleanValue("press.test");
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

	public static boolean isDebug(){
		return getBooleanValue("debug")||getBooleanValue("press.test");
	}

	public static boolean isFrontDebug(){
		return getBooleanValue("front.debug");
	}

	public static boolean logDetail(){
		return getBooleanValue("log.debug");
	}

	public static String getMch_id(){
		return getValue("mch_id");
	}

	public static String getExternalIP(){
		return getValue("external.ip");
	}

	public static Integer getBillOpenMinute(){
		return getIntValue("bill.open.minute");
	}

	public static String signupSuccessMsgKey(){
		return getValue("signup.success.msg");
	}

	public static String incompleteTaskMsgKey(){
		return getValue("incomplete.task.msg");
	}

	public static boolean messageSwitch(){
		return getBooleanValue("message.switch");
	}

	public static String coursePassMsgKey(){
		return getValue("course.pass.msg");
	}

	public static String domainName(){
		return getValue("app.domain");
	}

	public static String adapterDomainName(){
		return getValue("adapter.domain");
	}

	public static String resourceDomainName(){
		return getValue("resource.domain");
	}

	public static String streamResourceDomainName(){
		return getValue("stream.resource.domain");
	}

	public static String realDomainName(){
		return getValue("app.domainname");
	}

	public static String staticResourceUrl(){
		String url = getValue("static.resource.url");
		//测试环境防浏览器缓存，添加随机参数
		if(url.endsWith("?")){
			url = url.concat("_t=").concat(new Random().nextInt()+"");
		}

		return url;
	}

	public static String staticPcResourceUrl(){
		String url = getValue("static.pc.resource.url");
		//测试环境防浏览器缓存，添加随机参数
		if(url.endsWith("?")){
			url = url.concat("_t=").concat(new Random().nextInt()+"");
		}
		return url;
	}

	public static String gaId(){
		return getValue("ga.id");
	}

	public static String getDefaultOpenid(){
		return getValue("default.openid");
	}

	public static Integer getDefaultProfileId(){
		return getIntValue("default.profile.id");
	}

	/**
	 * 业务配置获取
	 */
	public static String getValue(String key){
		String value = null;
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

	public static Boolean getBooleanValue(String key){
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

	public static Integer getIntValue(String key){
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

	public static String getLoginSalt(){
		return getValue("login.salt");
	}

	public static String getLoginSocketUrl(){
		return getValue("static.pc.socket.url");
	}

	public static List<Integer> getWorkScoreList(){
		String scores = getValue("work.difficulty.score");
		String[] split = scores.split(",");
		List<Integer> list = Lists.newArrayList();
		for (String score : split) {
			list.add(Integer.valueOf(score));
		}
		return list;
	}

	public static Integer getChallengeScore(){
		return getIntValue("challenge.score");
	}

	public static Boolean isDevelopment(){
		return getBooleanValue("development");
	}

	public static Boolean isPcMaintenance(){
		return getBooleanValue("pc.server.maintenance");
	}

	public static String getSurveyUrl(Integer id){
        String json = getValue("wjx.survey");
        Map<String, Object> stringObjectMap = CommonUtils.jsonToMap(json);
        if (stringObjectMap.get(id + "") != null) {
            return (String) stringObjectMap.get(id + "");
        } else {
            return null;
        }
    }

	public static Integer getFeedBackId(){
		return getIntValue("wjx.feedback");
	}

	public static String willCloseMsgKey(){
		return getValue("will.close.task.msg");
	}

	public static String accountChangeMsgKey(){
		return getValue("account.change.msg");
	}

	public static Integer getProfileFullScore(){
		return getIntValue("profile.full.score");
	}

	public static String activityStartMsgKey(){
		return getValue("activity.start.msg");
	}

	public static String getUploadDomain(){
		return getValue("upload.image.domain");
	}

	public static String getPicturePrefix(){
		return getValue("qiniu.picture.prefix");
	}

	public static Date getRisePayStopTime(){
		return DateUtils.parseStringToDateTime(getValue("rise.member.pay.stop.time"));
	}

	public static Integer riseMemberTotal(){
		return getIntValue("rise.member.total.count");
	}

	public static String productPaidMsg() {
		return getValue("product.paid.msg");
	}

	public static String getIntegratedPracticeIndex(){
		return getValue("integrated.practice.index");
	}

	public static String getRabbitMQIp(){
		return getValue("rabbitmq.ip");
	}

	public static int getRabbitMQPort(){
		return getIntValue("rabbitmq.port");
	}

	public static String getRisePcAppid(){
		return getValue("rise.web.appid");
	}

	public static String getRisePcSecret() {
		return getValue("rise.web.secret");
	}

	public static Date getDiscountExpiredDate() {
		return DateUtils.parseStringToDateTime(getValue("operation.discount.expireddate"));
	}

	public static Integer getMinSendLimit(){
		return getIntValue("sms.min.send.limit");
	}

	public static Integer getHourSendLimit(){
		return getIntValue("sms.hour.send.limit");
	}

	public static Integer getDaySendLimit(){
		return getIntValue("sms.day.send.limit");
	}

	public static String getBizAccount(){
		return getValue("sms.business.account");
	}
	public static String getBizPassword(){
		return getValue("sms.business.password");
	}
	public static String getSMSSign(){
		return getValue("sms.sign");
	}
	public static String getMarketAccount(){
		return getValue("sms.market.account");
	}

	public static String getMarketPassword(){
		return getValue("sms.market.password");
	}


	public static SMSConfig getBizMsgConfig(){
		SMSConfig smsConfig = new SMSConfig();
		smsConfig.setAccount(getBizAccount());
		smsConfig.setPassword(getBizPassword());
		smsConfig.setSign(getSMSSign());
		return smsConfig;
	}

	public static SMSConfig getMarketMsgConfig(){
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

	public static Integer getVoteScore(){
		return getIntValue("vote.score");
	}
}
