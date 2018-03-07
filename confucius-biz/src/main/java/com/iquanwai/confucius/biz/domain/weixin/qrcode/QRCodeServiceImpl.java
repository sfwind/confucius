package com.iquanwai.confucius.biz.domain.weixin.qrcode;


import com.google.gson.Gson;
import com.iquanwai.confucius.biz.dao.wx.PromotionCodeDao;
import com.iquanwai.confucius.biz.domain.course.file.PictureService;
import com.iquanwai.confucius.biz.exception.UploadException;
import com.iquanwai.confucius.biz.po.PromotionQrCode;
import com.iquanwai.confucius.biz.util.*;
import com.iquanwai.confucius.biz.util.zk.ZKConfigUtils;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by justin on 16/8/12.
 */
@Service
public class QRCodeServiceImpl implements QRCodeService {
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private PromotionCodeDao promotionCodeDao;
    @Autowired
    private PictureService pictureService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    String SHOW_QRCODE_URL = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket={ticket}";

    @Override
    public QRResponse generateTemporaryQRCode(String scene, Integer expire_seconds) {
        if (expire_seconds == null) {
            expire_seconds = DEFAULT_EXPIRED_TIME;
        }
        QRTemporaryRequest qrRequest = new QRTemporaryRequest(scene, expire_seconds);
        String json = new Gson().toJson(qrRequest);
        QRResponse response = generate(json);

        //TODO:上传七牛云=>存入sql

        return response;
    }

    @Override
    public boolean checkScence(String scene) {
        if (promotionCodeDao.getByScene(scene) != null) {
            return true;
        }
        return false;
    }

    @Override
    public QRResponse generatePermanentQRCode(String scene) {
        QRPermanentRequest qrRequest = new QRPermanentRequest(scene);
        String json = new Gson().toJson(qrRequest);
        QRResponse response = generate(json);
        return response;
    }

    @Override
    public String loadQrBase64(String scene) {
        QRResponse response = generateTemporaryQRCode(scene, null);
        InputStream inputStream = showQRCode(response.getTicket());
        BufferedImage bufferedImage = ImageUtils.getBufferedImageByInputStream(inputStream);
        Assert.notNull(bufferedImage, "生成图片不能为空");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageUtils.writeToOutputStream(bufferedImage, "jpg", outputStream);
        BASE64Encoder encoder = new BASE64Encoder();
        try {
            return "data:image/jpg;base64," + encoder.encode(outputStream.toByteArray());
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                logger.error("os close failed", e);
            }
        }
    }

    @Override
    public String loadPerQrBase64(String scene,String remark) {
        String realName = CommonUtils.randomString(32)+".jpg";
        QRResponse response = generatePermanentQRCode(scene);
        InputStream inputStream = showQRCode(response.getTicket());
        BufferedImage bufferedImage = ImageUtils.getBufferedImageByInputStream(inputStream);
        Assert.notNull(bufferedImage, "生成图片不能为空");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageUtils.writeToOutputStream(bufferedImage, "jpg", outputStream);
        BASE64Encoder encoder = new BASE64Encoder();
        try {
            PromotionQrCode promotionQrCode = new PromotionQrCode();
            promotionQrCode.setScene(scene);
            promotionQrCode.setRemark(remark);

            ByteArrayInputStream swapStream = new ByteArrayInputStream(outputStream.toByteArray());

            boolean isSuccess = QiNiuUtils.uploadFile(realName, swapStream);
            if(isSuccess){
                promotionQrCode.setUrl(ConfigUtils.getPicturePrefix()+realName);
            }
            promotionCodeDao.insert(promotionQrCode);
            return "data:image/jpg;base64," + encoder.encode(outputStream.toByteArray());

        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                logger.error("os close failed", e);
            }
        }
    }

    private QRResponse generate(String json) {
        logger.info("json为:" + json);
        String body = restfulHelper.post(GEN_QRCODE_URL, json);
        System.out.println("return message " + body);
        Gson gson = new Gson();
        return gson.fromJson(body, QRResponse.class);
    }

    private InputStream showQRCode(String ticket) {
        String url = SHOW_QRCODE_URL.replace("{ticket}", ticket);
        ResponseBody body = restfulHelper.getPlain(url);
        if (body == null) {
            return null;
        }
        return body.byteStream();
    }

}
