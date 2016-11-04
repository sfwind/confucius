package com.iquanwai.confucius.web.weixin;

import com.iquanwai.confucius.biz.domain.weixin.signature.JsSignature;
import com.iquanwai.confucius.biz.domain.weixin.signature.JsSignatureService;
import com.iquanwai.confucius.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Map;

/**
 * Created by yangyuchen on 8/14/14.
 */
@RequestMapping("/wx/js")
@Controller
public class JsController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private JsSignatureService jsSignatureService;

    @RequestMapping("/signature")
    public ResponseEntity<Map<String, Object>> signature(@RequestParam("url") String url) throws IOException {
        try {
            JsSignature jsSignature = jsSignatureService.getJsSignature(url, false);
            return WebUtils.result(jsSignature);
        }catch (Exception e){
            LOGGER.error("js signature failed", e);
        }
        return WebUtils.error("js signature failed");
    }

}
