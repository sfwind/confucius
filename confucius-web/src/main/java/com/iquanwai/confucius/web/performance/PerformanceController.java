package com.iquanwai.confucius.web.performance;

import com.iquanwai.confucius.biz.domain.performance.PerformanceService;
import com.iquanwai.confucius.biz.domain.performance.entity.PageAnalyticsDto;
import com.iquanwai.confucius.web.performance.dto.PerformanceSourceInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


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
    public ResponseEntity<PageAnalyticsDto> queryLineChartData(String app, String beginTimeStr,String endTimeStr,Integer unitTimeAboutMinutes) {
        PageAnalyticsDto result = new PageAnalyticsDto();
        try {
            if(unitTimeAboutMinutes == null) {
               unitTimeAboutMinutes = 1;
            }
            result = performanceService.queryLineChartData(app, beginTimeStr, endTimeStr, unitTimeAboutMinutes);
        } catch (Exception e) {
            logger.error("performanceService.queryLineChartData error", e);
            return new ResponseEntity<PageAnalyticsDto>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<PageAnalyticsDto>(result, HttpStatus.OK);
    }

}
