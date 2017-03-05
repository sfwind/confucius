package com.iquanwai.confucius.biz.domain.performance;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.performance.PagePerformanceDao;
import com.iquanwai.confucius.biz.domain.performance.entity.DataSourceForPoint;
import com.iquanwai.confucius.biz.domain.performance.entity.PageAnalyticsDto;
import com.iquanwai.confucius.biz.domain.performance.entity.Point;
import com.iquanwai.confucius.biz.po.performance.PagePerformance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

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
    public PageAnalyticsDto queryLineChartData(String app, String startTimeStr, String endTimeStr, int unitTimeAboutMinutes) {
        PageAnalyticsDto pageAnalyticsDto = new PageAnalyticsDto();
        List<PagePerformance> pagePerformanceList = pagePerformanceDao.queryAboutAddTime(app, startTimeStr, endTimeStr);
        //按照时间把数据分段
        List<DataSourceForPoint> dataSourceForPoints = Lists.newArrayList();
        LocalDateTime pointStartTime = null;
        LocalDateTime pointEndTime = null;
        List<PagePerformance> pagePerformances = null;
        ZoneId currentZone = ZoneId.systemDefault();
        for (int i = 0; i < pagePerformanceList.size(); i++) {
            PagePerformance po = pagePerformanceList.get(i);
            Instant addTimeInstall = po.getAddTime().toInstant();
            LocalDateTime addTime = LocalDateTime.ofInstant(addTimeInstall, currentZone);
            if (pointStartTime == null) {
                pointStartTime = LocalDateTime.of(addTime.getYear(), addTime.getMonth(), addTime.getDayOfMonth(), addTime.getHour(), addTime.getMinute());
                pointEndTime = pointStartTime.plusMinutes(unitTimeAboutMinutes);
                pagePerformances = Lists.newArrayList(po);
            } else if (addTime.isBefore(pointEndTime)) {
                pagePerformances.add(po);
            } else {
                DataSourceForPoint dataSourceForPoint = new DataSourceForPoint(pagePerformances, pointStartTime);
                dataSourceForPoints.add(dataSourceForPoint);
                if (addTime.isAfter(pointEndTime.plusMinutes(unitTimeAboutMinutes))) {
                    pointStartTime = LocalDateTime.of(addTime.getYear(), addTime.getMonth(), addTime.getDayOfMonth(), addTime.getHour(), addTime.getMinute());
                    pointEndTime = pointStartTime.plusMinutes(unitTimeAboutMinutes);
                } else {
                    pointStartTime = pointEndTime;
                    pointEndTime = pointStartTime.plusMinutes(unitTimeAboutMinutes);
                }
                pagePerformances = Lists.newArrayList(po);
            }
        }
        dataSourceForPoints.add(new DataSourceForPoint(pagePerformances, pointStartTime));
        //数据转化
        List<Point> pvList = Lists.newArrayList();
        List<Point> loadList = Lists.newArrayList();
        List<Point> interactiveList = Lists.newArrayList();
        List<Point> ttfbList = Lists.newArrayList();
        List<Point> paintList = Lists.newArrayList();
        for (int i = 0; i < dataSourceForPoints.size(); i++) {
            DataSourceForPoint dataPo = dataSourceForPoints.get(i);
            long xTime = dataPo.getTime().atZone(currentZone).toEpochSecond() * 1000;
            //获取访问量数据
            Point pv = new Point();
            pv.setX(xTime);
            pv.setY(dataPo.getPagePerformances().size());
            pvList.add(pv);
            //获取完全加载时间
            Point load = new Point();
            Double averagingLoad = dataPo.getPagePerformances().stream().collect(Collectors.averagingInt(PagePerformance::getLoad));
            load.setY(averagingLoad.longValue());
            load.setX(xTime);
            loadList.add(load);
            //获取可交互时间
            Point interactive = new Point();
            Double averagingInteractive = dataPo.getPagePerformances().stream().collect(Collectors.averagingInt(PagePerformance::getInteractive));
            interactive.setY(averagingInteractive.longValue());
            interactive.setX(xTime);
            interactiveList.add(interactive);
            //获取首字节时间
            Point ttfb = new Point();
            Double averagingTtfb = dataPo.getPagePerformances().stream().collect(Collectors.averagingInt(PagePerformance::getTtfb));
            ttfb.setY(averagingTtfb.longValue());
            ttfb.setX(xTime);
            ttfbList.add(ttfb);

            //获取白屏时间
            Point paint = new Point();
            Double averagingPaint = dataPo.getPagePerformances().stream().collect(Collectors.averagingInt(PagePerformance::getFirstPaint));
            paint.setY(averagingPaint.longValue());
            paint.setX(xTime);
            paintList.add(paint);
        }
        pageAnalyticsDto.setPvList(pvList);
        pageAnalyticsDto.setInteractiveList(interactiveList);
        pageAnalyticsDto.setLoadList(loadList);
        pageAnalyticsDto.setTtfbList(ttfbList);
        pageAnalyticsDto.setPaintList(paintList);
        return pageAnalyticsDto;
    }
}
