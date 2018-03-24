package com.iquanwai.confucius.web.backend;

import com.iquanwai.confucius.biz.domain.backend.FlowDataService;
import com.iquanwai.confucius.biz.po.RichText;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by 三十文
 */
@RestController
@RequestMapping("/backend/flow")
public class FlowDataController {

    @Autowired
    private FlowDataService flowDataService;

    @RequestMapping(value = "/richText", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> uploadRichText(@RequestParam("title") String title, @RequestParam("content") String content) {
        RichText richText = flowDataService.insertRichText(title, content);
        if (richText == null) {
            return WebUtils.error("富文本存储错误，请重试");
        } else {
            return WebUtils.result(richText.getUuid());
        }
    }


}
