package com.iquanwai.confucius.biz.domain.weixin.qrcode;

/**
 * Created by justin on 16/8/12.<br/>
 * Description: 操作二维码
 */
public interface QRCodeService {

    /**
     * 检查scene是否重复
     * @param scene
     * @return
     */
    boolean checkScence(String scene);

    /**
     * 生成永久二维码
     *
     * @param scene 场景值
     * @return 二维码内容
     */
    QRResponse generatePermanentQRCode(String scene);

    /**
     * 生成临时二维码
     *
     * @param scene          场景值
     * @param expire_seconds 过期时间
     * @return 二维码内容
     */
    QRResponse generateTemporaryQRCode(String scene, Integer expire_seconds);

    String loadQrBase64(String scene);

    /* 获取二维码的链接 */
    String GEN_QRCODE_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token={access_token}";

    /* 默认过期时间 */
    int DEFAULT_EXPIRED_TIME = 60 * 60 * 24 * 30;
}
