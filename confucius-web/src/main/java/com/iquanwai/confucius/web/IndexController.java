package com.iquanwai.confucius.web;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.web.resolver.UnionUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private OperationLogService operationLogService;

    private static final String PAY_VIEW = "pay";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/pay/alipay/**", method = RequestMethod.GET)
    public ModelAndView getAlipayIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
        OperationLog operationLog = new OperationLog().function("打点").module("访问页面").action("阿里支付").memo(request.getRequestURI());
        operationLogService.log(operationLog);
        return payView(request, PAY_VIEW);
    }

    @RequestMapping(value = "/pay/**", method = RequestMethod.GET)
    public ModelAndView getPayIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return payView(request, PAY_VIEW);
    }

    @RequestMapping(value = "/subscribe")
    public ModelAndView goSubscribe(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("用户未关注，跳转关注页面：{}", request.getRequestURI());
        return payView(request, PAY_VIEW);
    }

    @RequestMapping(value = "/heartbeat", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> heartbeat() throws Exception {
        return WebUtils.success();
    }

    private ModelAndView payView(HttpServletRequest request, String viewName) {
        ModelAndView mav = new ModelAndView(viewName);
        String domainName = request.getHeader("Host-Test");
        String resource = ConfigUtils.staticPayUrl(domainName);

        if (request.getParameter("debug") != null) {
            if (ConfigUtils.isFrontDebug()) {
                mav.addObject("resource", "http://0.0.0.0:4000/pay_bundle.js");
            } else {
                mav.addObject("resource", resource);
            }
        } else {
            mav.addObject("resource", resource);
        }

        if (unionUser != null) {
            Map<String, String> userParam = Maps.newHashMap();
            userParam.put("userName", unionUser.getNickName());
            if (unionUser.getHeadImgUrl() != null) {
                userParam.put("headImage", unionUser.getHeadImgUrl().replace("http:", "https:"));
            }
            mav.addAllObjects(userParam);
        }
        return mav;
    }
}
