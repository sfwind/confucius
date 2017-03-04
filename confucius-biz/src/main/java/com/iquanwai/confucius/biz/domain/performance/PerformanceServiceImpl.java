package com.iquanwai.confucius.biz.domain.performance;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.performance.PagePerformanceDao;
import com.iquanwai.confucius.biz.po.performance.DataSourceForPoint;
import com.iquanwai.confucius.biz.po.performance.PagePerformance;
import com.iquanwai.confucius.biz.po.performance.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by shen on 17/3/2.
 */
@Service
public class PerformanceServiceImpl implements PerformanceService {
    @Autowired
    private PagePerformanceDao pagePerformanceDao;
    @Override
    public void add(PagePerformance po) {
        pagePerformanceDao.entry(po);
    }

    @Override
    public Map<String,List<Point>> queryLineChartData(String startTimeStr, String endTimeStr, int unitTimeAboutMinutes) {
        Map<String,List<Point>>  lineChartDataMap = Maps.newConcurrentMap();
        List<PagePerformance> pagePerformanceList = pagePerformanceDao.queryAboutAddTime(startTimeStr, endTimeStr);
        //按照时间把数据分段
        List<DataSourceForPoint> dataSourceForPoints = Lists.newArrayList();
        LocalDateTime pointStartTime = null;
        LocalDateTime pointEndTime = null;
        List<PagePerformance> pagePerformances = null;
        ZoneId currentZone = ZoneId.systemDefault();
        for(int i= 0; i<pagePerformanceList.size(); i++){
            PagePerformance po = pagePerformanceList.get(i);
            Instant addTimeInstall = po.getAddTime().toInstant();

            LocalDateTime addTime = LocalDateTime.ofInstant(addTimeInstall, currentZone);
            if(pointStartTime == null){
                pointStartTime = LocalDateTime.of(addTime.getYear(),addTime.getMonth(),addTime.getDayOfMonth(),addTime.getHour(),addTime.getMinute());
                pointEndTime = pointStartTime.plusMinutes(unitTimeAboutMinutes);
                pagePerformances = Lists.newArrayList(po);
            }else if(addTime.isBefore(pointEndTime)){
                pagePerformances.add(po);
            }else {
                DataSourceForPoint dataSourceForPoint = new DataSourceForPoint(pagePerformances,pointStartTime);
                dataSourceForPoints.add(dataSourceForPoint);
                if(addTime.isAfter(pointEndTime.plusMinutes(unitTimeAboutMinutes))){
                    pointStartTime = LocalDateTime.of(addTime.getYear(),addTime.getMonth(),addTime.getDayOfMonth(),addTime.getHour(),addTime.getMinute());
                    pointEndTime = pointStartTime.plusMinutes(unitTimeAboutMinutes);
                } else {
                    pointStartTime = pointEndTime;
                    pointEndTime = pointStartTime.plusMinutes(unitTimeAboutMinutes);
                }
                pagePerformances = Lists.newArrayList(po);
            }
        }
        dataSourceForPoints.add(new DataSourceForPoint(pagePerformances,pointStartTime));
        //数据转化
        List<Point> pvList = Lists.newArrayList();
        for(int i= 0; i<dataSourceForPoints.size(); i++){
            DataSourceForPoint dataPo = dataSourceForPoints.get(i);
            //获取访问量数据
            Point pv = new Point();
            pv.setX(dataPo.getTime().atZone(currentZone).toEpochSecond() * 1000);
            pv.setY(dataPo.getPagePerformances().size());
            pvList.add(pv);
            //todo 其他性能数据

        }
        lineChartDataMap.put("pvList", pvList);
        return lineChartDataMap;
    }
}
