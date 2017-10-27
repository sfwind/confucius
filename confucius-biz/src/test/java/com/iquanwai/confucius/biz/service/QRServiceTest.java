package com.iquanwai.confucius.biz.service;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.domain.weixin.message.customer.CustomerMessageService;
import com.iquanwai.confucius.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.confucius.biz.domain.weixin.qrcode.QRResponse;
import com.iquanwai.confucius.biz.domain.weixin.tag.TagDto;
import com.iquanwai.confucius.biz.domain.weixin.tag.TagService;
import com.iquanwai.confucius.biz.util.Constants;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by justin on 17/7/8.
 */
public class QRServiceTest extends TestBase {
    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private TagService tagService;

    /**
     * 生成二维码测试
     */
    @Test
    public void generateCode(){
//        qrCodeService.generatePermanentQRCode("camp_month_10");
        QRResponse futt_live = qrCodeService.generateTemporaryQRCode("futt_live_2", 2592000);
//        qrCodeService.generateTemporaryQRCode("freeLimit8", 2592000);
//        QRResponse courseReduction_zlj = qrCodeService.generateTemporaryQRCode("courseReduction-zlj_2", 2592000);
//        System.out.println(JSON.toJSONString(caitonglive_rise));
    }
    @Test
    public void sendTest(){
        customerMessageService.sendCustomerMessage("o-Es21bZakuqjBfVr7a-_j90WQuI", "8KnD1B4suq3dLz9H5ACHTydrC3_k2sftW-fcJhWhjQqzxi7uMMyU7BdGyYTYnQES", Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
    }

    @Test
    public void tagTest(){
        TagDto tagDto = new TagDto();
        tagDto.setTagid(101);
        List<String> openids = Lists.newArrayList();
        openids.add("o-Es21RVF3WCFQMOtl07Di_O9NVo");
        tagDto.setOpenid_list(openids);
        tagService.removeTag(tagDto);
    }
}
