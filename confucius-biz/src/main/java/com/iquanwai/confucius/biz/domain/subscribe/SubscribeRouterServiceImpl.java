package com.iquanwai.confucius.biz.domain.subscribe;

import com.iquanwai.confucius.biz.dao.common.customer.SubscribeRouterConfigDao;
import com.iquanwai.confucius.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.confucius.biz.po.common.customer.SubscribeRouterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class SubscribeRouterServiceImpl implements SubscribeRouterService {

    @Autowired
    private SubscribeRouterConfigDao subscribeRouterConfigDao;
    @Autowired
    private QRCodeService qrCodeService;


    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public SubscribeRouterConfig loadUnSubscribeRouterConfig(String currentPatchName, String followKey) {

        List<SubscribeRouterConfig> routerConfigs = subscribeRouterConfigDao.loadAll();

        SubscribeRouterConfig targetSubscribeRouterConfig = null;

        for (SubscribeRouterConfig routerConfig : routerConfigs) {
            try {
                String urlRegex = routerConfig.getUrl();
                logger.info("开始解析：" + urlRegex);
                boolean isMatchUri = Pattern.matches(urlRegex, currentPatchName);
                boolean isMatchKey = followKey == null || followKey.equals(routerConfig.getScene());
                if (isMatchUri && isMatchKey) {
                    // uri和key都匹配到，则返回
                    targetSubscribeRouterConfig = routerConfig;
                    break;
                }
                if (isMatchUri && targetSubscribeRouterConfig == null) {
                    // uri匹配到，并且target为null 先设置一个返回值, 保证有匹配的url
                    targetSubscribeRouterConfig = routerConfig;
                }
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        return targetSubscribeRouterConfig;
    }

    @Override
    public String loadSubscribeQrCode(String scene) {
        return qrCodeService.loadQrBase64(scene);
    }

    @Override
    public String loadPerSubscribeQrCode(String scene) {
        return qrCodeService.loadPerQrBase64(scene);
    }

}
