package com.iquanwai.confucius.web.performance;

import com.iquanwai.confucius.biz.domain.performance.PerformanceService;
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
    public ResponseEntity<String> loadProfile(PerformanceSourceInfoDto performanceSourceInfoDto) {
        try {
            performanceService.add(performanceSourceInfoDto.toPo());
        } catch (Exception e) {
            logger.error("beanUtils copy props error", e);
            return new ResponseEntity<String>("数据收集异常", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>("", HttpStatus.OK);
    }

}
