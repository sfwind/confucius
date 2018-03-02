package com.iquanwai.confucius.web.weixin;

import com.iquanwai.confucius.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.confucius.biz.domain.weixin.qrcode.QRResponse;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by justin on 16/8/13.
 */
@RequestMapping("/wx/qrcode")
@Controller
public class QRCodeController {
    @Autowired
    private QRCodeService qrCodeService;

    @RequestMapping("/{scene}")
    public ResponseEntity<Map<String, Object>> generate(@PathVariable String scene,
                                                        @RequestParam(name="p", required = false) String permanent,
                                                        @RequestParam(name="s", required = false) Integer seconds) {
        QRResponse response;
        if(permanent!=null){
            response = qrCodeService.generatePermanentQRCode(scene);
        }else{
            response = qrCodeService.generateTemporaryQRCode(scene, seconds);
        }
        if(response.getTicket()!=null){

        }

        return WebUtils.result(response);
    }

}
