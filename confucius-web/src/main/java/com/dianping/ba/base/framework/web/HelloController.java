package com.dianping.ba.base.framework.web;

import com.dianping.ba.base.framework.biz.domain.hello.HelloDto;
import com.dianping.ba.base.framework.biz.domain.hello.HelloManager;
import com.dianping.ba.base.framework.biz.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;


/**
 * Created by justin on 7/23/15.
 */
@Controller
@RequestMapping("/hello")
public class HelloController {
    @Autowired
    private HelloManager helloManager;

    @RequestMapping(value="/test1", produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String printWelcome(ModelMap model, @RequestParam("id") String employeeNumber) {
        HelloDto helloDto = new HelloDto();
        helloDto.setEmployeeNumber(employeeNumber);

        return helloManager.hello(helloDto);
    }

    @RequestMapping(value="/test2")
    public ResponseEntity<Map<String, Object>> printJson(ModelMap model,
                                                         @RequestParam("id") String employeeNumber) {
        HelloDto helloDto = new HelloDto();
        helloDto.setEmployeeNumber(employeeNumber);

        return WebUtil.result(helloManager.hello(helloDto));
    }
}
