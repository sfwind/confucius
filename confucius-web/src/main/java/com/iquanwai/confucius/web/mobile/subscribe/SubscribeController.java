package com.iquanwai.confucius.web.mobile.subscribe;

import com.iquanwai.confucius.biz.domain.subscribe.SubscribeRouterService;
import com.iquanwai.confucius.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController()
@RequestMapping("/subscribe")
public class SubscribeController {

    @Autowired
    private SubscribeRouterService subscribeRouterService;
    @Autowired
    private QRCodeService qrCodeService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/qrCode")
    public ResponseEntity<Map<String, Object>> loadQrCode(@RequestParam("scene") String scene) {
        String qrCodeBase64 = subscribeRouterService.loadSubscribeQrCode(scene);
        if (qrCodeBase64 == null) {
            return WebUtils.error("当前场景值不存在");
        }
        return WebUtils.result(qrCodeBase64);
    }

    @RequestMapping("/per/qrCode")
    public ResponseEntity<Map<String,Object>> loadPerQrCode(@RequestParam("scene")String scene,@RequestParam("remark") String remark){
        if(qrCodeService.checkScence(scene)){
            return WebUtils.error("场景值重复");
        }

        String qrCodeBase64 = subscribeRouterService.loadPerSubscribeQrCode(scene,remark);
        if (qrCodeBase64 == null) {
            return WebUtils.error("当前场景值不存在");
        }
        return WebUtils.result(qrCodeBase64);
    }

}
