package com.iquanwai.confucius.biz.service;

import com.alibaba.fastjson.JSON;
import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.domain.weixin.message.customer.CustomerMessageService;
import com.iquanwai.confucius.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.confucius.biz.domain.weixin.qrcode.QRResponse;
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

    /**
     * 生成二维码测试
     */
    @Test
    public void generateCode(){
//        qrCodeService.generatePermanentQRCode("museum_ma");
//        qrCodeService.generateTemporaryQRCode("freeLimit_RISE_9", 2592000);
//        qrCodeService.generateTemporaryQRCode("freeLimit8", 2592000);
        QRResponse courseReduction_zlj = qrCodeService.generateTemporaryQRCode("courseReduction-zlj_1", 2592000);
        System.out.println(JSON.toJSONString(courseReduction_zlj));
    }
    @Test
    public void sendTest(){
        customerMessageService.sendCustomerMessage("o-Es21bZakuqjBfVr7a-_j90WQuI", "8KnD1B4suq3dLz9H5ACHTydrC3_k2sftW-fcJhWhjQqzxi7uMMyU7BdGyYTYnQES", Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
    }
}
