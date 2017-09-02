package com.iquanwai.confucius.biz.util;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.util.zk.ZKConfigUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
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

	public static boolean isDebug(){
		return getBooleanValue("debug");
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

	public static Double getDoubleValue(String key){
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
}
