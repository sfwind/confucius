package com.iquanwai.confucius.web.util;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by justin on 7/23/15.
 */
public class WebUtils {
    private static Logger logger = LoggerFactory.getLogger(WebUtils.class);

    public static ResponseEntity<Map<String, Object>> success() {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", 200);
        json.put("msg", "ok");

        return new ResponseEntity<Map<String, Object>>(json, HttpStatus.OK);
    }

    public static ResponseEntity<Map<String, Object>> result(Object result) {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", 200);
        json.put("msg", result);

        return new ResponseEntity<Map<String, Object>>(json, HttpStatus.OK);
    }

    public static ResponseEntity<Map<String, Object>> error(Object msg) {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", 221);
        json.put("msg", msg);

        return new ResponseEntity<Map<String, Object>>(json, HttpStatus.OK);
    }

    public static ResponseEntity<Map<String, Object>> error(int code, Object msg) {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", code);
        json.put("msg", msg);

        return new ResponseEntity<Map<String, Object>>(json, HttpStatus.OK);
    }

    public static ResponseEntity<Map<String, Object>> error(int code, Object msg, HttpStatus status) {
        Map<String, Object> json = Maps.newHashMap();
        json.put("code", code);
        json.put("msg", msg);

        return new ResponseEntity<Map<String, Object>>(json, status);
    }

    public static void auth(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String url = request.getRequestURL().toString();
        if (!StringUtils.isEmpty(request.getQueryString())) {
            url = url + "?" + request.getQueryString();
        }
        url = URLEncoder.encode(url, "UTF-8");

        String domainName = request.getHeader("Domain-Name");

        if (domainName != null) {
            response.sendRedirect("http://" + domainName + "/wx/oauth/auth?callbackUrl=" + url);
        } else {
            response.sendRedirect(ConfigUtils.adapterDomainName() + "/wx/oauth/auth?callbackUrl=" + url);
        }
    }

    /**
     * pc端，跳转至登录页面
     */
    public static void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String url = request.getRequestURL().toString();
        if (!StringUtils.isEmpty(request.getQueryString())) {
            url = url + "?" + request.getQueryString();
        }
        url = URLEncoder.encode(url, "UTF-8");
        response.sendRedirect(ConfigUtils.adapterDomainName() + "/login?callbackUrl=" + url);
    }

    /**
     * TODO 跳转至拒绝页面
     */
    public static void reject(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.sendRedirect(ConfigUtils.adapterDomainName() + "/pc/static/reject");
    }

    /**
     * TODO 跳转至异常页面
     */
    public static void redirectError(HttpServletRequest request, HttpServletResponse response, String err) throws Exception {
        err = URLEncoder.encode(err, "UTF-8");
        response.sendRedirect(ConfigUtils.adapterDomainName() + "/pc/static/error?err=" + err);
    }

    /**
     * 普通跳转
     */
    public static void redirect(HttpServletRequest request, HttpServletResponse response, String uri) throws Exception {
        response.sendRedirect(ConfigUtils.adapterDomainName() + uri);
    }

    /**
     * 跳转至问卷星
     */
    public static void wjx(HttpServletRequest request,HttpServletResponse response,String url) throws IOException {
        response.sendRedirect(url);
    }
}
