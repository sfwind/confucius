package com.iquanwai.confucius.biz.service;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.domain.weixin.qrcode.QRCodeService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 17/7/8.
 */
public class QRServiceTest extends TestBase {
    @Autowired
    private QRCodeService qrCodeService;

    @Test
    public void generateCode(){
        qrCodeService.generatePermanentQRCode("promotion");
        qrCodeService.generateTemporaryQRCode("test", 2592000);
    }
}
