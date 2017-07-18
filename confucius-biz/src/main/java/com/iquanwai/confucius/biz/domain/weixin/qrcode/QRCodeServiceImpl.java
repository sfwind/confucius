package com.iquanwai.confucius.biz.domain.weixin.qrcode;

import com.google.gson.Gson;
import com.iquanwai.confucius.biz.util.RestfulHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by justin on 16/8/12.
 */
@Service
public class QRCodeServiceImpl implements QRCodeService {
    @Autowired
    private RestfulHelper restfulHelper;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public QRResponse generateTemporaryQRCode(String scene, Integer expire_seconds) {
        if(expire_seconds==null){
            expire_seconds = DEFAULT_EXPIRED_TIME;
        }
        QRTemporaryRequest qrRequest = new QRTemporaryRequest(scene, expire_seconds);
        String json = new Gson().toJson(qrRequest);
        return generate(json);
    }

    public QRResponse generatePermanentQRCode(String scene) {
        QRPermanentRequest qrRequest = new QRPermanentRequest(scene);
        String json = new Gson().toJson(qrRequest);
        return generate(json);
    }

    private QRResponse generate(String json) {
        String body = restfulHelper.post(GEN_QRCODE_URL, json);
        System.out.println("return message " + body);
        Gson gson = new Gson();
        return gson.fromJson(body, QRResponse.class);
    }
}
