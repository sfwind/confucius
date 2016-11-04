package com.iquanwai.confucius.web.weixin;

import com.iquanwai.confucius.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.confucius.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Created by justin on 16/8/13.
 */
@RequestMapping("/wx/qrcode")
@Controller
public class QRCodeController {
    @Autowired
    private QRCodeService qrCodeService;

    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @RequestMapping("/{scene}")
    public ResponseEntity<Map<String, Object>> generate(@PathVariable String scene) {
        try {
            String url = qrCodeService.generateQRCode(scene);
            return WebUtils.result(url);
        }catch (Exception e){
            LOGGER.error("qrcode failed", e);
        }
        return WebUtils.error("qrcode failed");
    }
}
