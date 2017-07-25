package com.iquanwai.confucius.biz.service;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.domain.weixin.message.customer.CustomerMessageService;
import com.iquanwai.confucius.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.confucius.biz.util.Constants;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 17/7/8.
 */
public class QRServiceTest extends TestBase {
    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private CustomerMessageService customerMessageService;

    @Test
    public void generateCode(){
//        qrCodeService.generatePermanentQRCode("promotion");
        qrCodeService.generateTemporaryQRCode("freeLimit_8", 2592000);
//        qrCodeService.generateTemporaryQRCode("freeLimit8", 2592000);
    }
    @Test
    public void sendTest(){
        customerMessageService.sendCustomerMessage("o-Es21bZakuqjBfVr7a-_j90WQuI", "8KnD1B4suq3dLz9H5ACHTydrC3_k2sftW-fcJhWhjQqzxi7uMMyU7BdGyYTYnQES", Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
    }
}
