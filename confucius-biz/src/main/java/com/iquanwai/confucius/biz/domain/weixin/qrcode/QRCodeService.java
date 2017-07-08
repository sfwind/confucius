package com.iquanwai.confucius.biz.domain.weixin.qrcode;

/**
 * Created by justin on 16/8/12.
 */
public interface QRCodeService {
    QRResponse generatePermanentQRCode(String scene);

    QRResponse generateTemporaryQRCode(String scene, int expire_seconds);

    String GEN_QRCODE_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token={access_token}";
}
