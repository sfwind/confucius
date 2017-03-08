package com.iquanwai.confucius.web.performance.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.performance.PerformanceService;
import com.iquanwai.confucius.biz.domain.performance.entity.PageAnalyticsDto;
import com.iquanwai.confucius.biz.domain.performance.entity.PersonalAnalyticsDto;
import com.iquanwai.confucius.biz.po.performance.PagePerformance;
import com.iquanwai.confucius.biz.po.performance.PageUrl;
import com.iquanwai.confucius.biz.po.performance.PersonalPerfKey;
import com.iquanwai.confucius.biz.po.performance.PersonalPerformance;
import com.iquanwai.confucius.web.performance.dto.PerformanceSourceInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;


/**
 * Created by yongqiang.shen on 2017/3/2.
 */
@RestController
@RequestMapping("/performance")
public class PerformanceController {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PerformanceService performanceService;

    @RequestMapping(value = "/report", method = RequestMethod.GET)
    public ResponseEntity<String> reportPerformanceData(PerformanceSourceInfoDto performanceSourceInfoDto) {
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.setAccessControlAllowOrigin("*");
            headers.setAccessControlAllowMethods(Lists.newArrayList(HttpMethod.GET));
            performanceSourceInfoDto.mapPo();
            PagePerformance pagePerformance = performanceSourceInfoDto.getPagePerformance();
            if(pagePerformance!=null) {
                performanceService.add(performanceSourceInfoDto.getPagePerformance());
            }
            PersonalPerformance personalPerformance = performanceSourceInfoDto.getPersonalPerformance();
            if(personalPerformance!=null){
                performanceService.addPersonalPerf(performanceSourceInfoDto.getPersonalPerformance());
            }
        } catch (Exception e) {
            logger.error("performanceService.add error", e);
            return new ResponseEntity<String>("数据收集异常", headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>("", headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/queryDataAboutLineChart", method = RequestMethod.GET)
    public ResponseEntity<PageAnalyticsDto> queryLineChartData(Integer urlId, String beginTimeStr, String endTimeStr, Integer unitTimeAboutMinutes) {
        PageAnalyticsDto result = new PageAnalyticsDto();
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.setAccessControlAllowOrigin("*");
            headers.setAccessControlAllowMethods(Lists.newArrayList(HttpMethod.GET));
            Assert.notNull(urlId, "urlId 不能为空");
            if (unitTimeAboutMinutes == null) {
                unitTimeAboutMinutes = 1;
            }
            result = performanceService.queryLineChartData(urlId, beginTimeStr, endTimeStr, unitTimeAboutMinutes);
        } catch (Exception e) {
            logger.error("performanceService.queryLineChartData error", e);
            return new ResponseEntity<PageAnalyticsDto>(result, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<PageAnalyticsDto>(result, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/queryPersonalDataAboutLineChart", method = RequestMethod.GET)
    public ResponseEntity<PersonalAnalyticsDto> queryPersonalLineChartData(Integer keyId, String beginTimeStr, String endTimeStr, Integer unitTimeAboutMinutes) {
        PersonalAnalyticsDto result = new PersonalAnalyticsDto();
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.setAccessControlAllowOrigin("*");
            headers.setAccessControlAllowMethods(Lists.newArrayList(HttpMethod.GET));
            Assert.notNull(keyId, "keyId 不能为空");
            if (unitTimeAboutMinutes == null) {
                unitTimeAboutMinutes = 1;
            }
            result = performanceService.queryPersonalLineChartData(keyId, beginTimeStr, endTimeStr, unitTimeAboutMinutes);
        } catch (Exception e) {
            logger.error("performanceService.queryLineChartData error", e);
            return new ResponseEntity<PersonalAnalyticsDto>(result, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<PersonalAnalyticsDto>(result, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/queryUrlList", method = RequestMethod.GET)
    public ResponseEntity<List<PageUrl>> queryUrlList() {
        List<PageUrl> result = Lists.newArrayList();
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.setAccessControlAllowOrigin("*");
            headers.setAccessControlAllowMethods(Lists.newArrayList(HttpMethod.GET));
            result = performanceService.queryUrlList();
        } catch (Exception e) {
            logger.error("performanceService.queryLineChartData error", e);
            return new ResponseEntity<List<PageUrl>>(result, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<List<PageUrl>>(result, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/queryKeyList", method = RequestMethod.GET)
    public ResponseEntity<List<PersonalPerfKey>> queryKeyList() {
        List<PersonalPerfKey> result = Lists.newArrayList();
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.setAccessControlAllowOrigin("*");
            headers.setAccessControlAllowMethods(Lists.newArrayList(HttpMethod.GET));
            result = performanceService.queryKeyList();
        } catch (Exception e) {
            logger.error("performanceService.queryLineChartData error", e);
            return new ResponseEntity<List<PersonalPerfKey>>(result, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<List<PersonalPerfKey>>(result, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "")
    public ModelAndView goPerformancePage(){
        ModelAndView mav = new ModelAndView("performance");
        return mav;
    }

}
