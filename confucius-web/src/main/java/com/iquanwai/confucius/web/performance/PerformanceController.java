package com.iquanwai.confucius.web.performance;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.domain.performance.PerformanceService;
import com.iquanwai.confucius.biz.po.performance.Point;
import com.iquanwai.confucius.web.performance.dto.PerformanceSourceInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
        try {
            performanceService.add(performanceSourceInfoDto.toPo());
        } catch (Exception e) {
            logger.error("performanceService.add error", e);
            return new ResponseEntity<String>("数据收集异常", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>("", HttpStatus.OK);
    }

    @RequestMapping(value = "/queryDataAboutLineChart", method = RequestMethod.GET)
    public ResponseEntity<Map<String,List<Point>>> queryLineChartData(String beginTimeStr,String endTimeStr,Integer unitTimeAboutMinutes) {
        Map<String,List<Point>> dataMap = Maps.newConcurrentMap();
        try {
            if(unitTimeAboutMinutes == null) {
               unitTimeAboutMinutes = 1;
            }
            dataMap = performanceService.queryLineChartData(beginTimeStr, endTimeStr, unitTimeAboutMinutes);
        } catch (Exception e) {
            logger.error("performanceService.queryLineChartData error", e);
            return new ResponseEntity<Map<String,List<Point>>>(dataMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<Map<String,List<Point>>>(dataMap, HttpStatus.OK);
    }

}
