package com.iquanwai.confucius.biz.domain.performance;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.performance.PagePerformanceDao;
import com.iquanwai.confucius.biz.dao.performance.PageUrlDao;
import com.iquanwai.confucius.biz.dao.performance.PersonalPerfKeyDao;
import com.iquanwai.confucius.biz.dao.performance.PersonalPerformanceDao;
import com.iquanwai.confucius.biz.domain.performance.entity.DataSourceForPoint;
import com.iquanwai.confucius.biz.domain.performance.entity.PageAnalyticsDto;
import com.iquanwai.confucius.biz.domain.performance.entity.PersonalAnalyticsDto;
import com.iquanwai.confucius.biz.domain.performance.entity.Point;
import com.iquanwai.confucius.biz.po.performance.PagePerformance;
import com.iquanwai.confucius.biz.po.performance.PageUrl;
import com.iquanwai.confucius.biz.po.performance.PersonalPerfKey;
import com.iquanwai.confucius.biz.po.performance.PersonalPerformance;
import com.iquanwai.confucius.biz.po.systematism.Page;
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

    @Autowired
    private PageUrlDao pageUrlDao;

    @Autowired
    private PersonalPerformanceDao personalPerformanceDao;

    @Autowired
    private PersonalPerfKeyDao personalPerfKeyDao;

    @Override
    public void add(PagePerformance po) {
        pagePerformanceDao.entry(po);
        PageUrl pageUrl = pageUrlDao.getByUrl(po.getUrl());
        if(pageUrl == null || pageUrl.getUrl() == null || "".equals(pageUrl.getUrl())){
            pageUrl = new PageUrl();
            pageUrl.setUrl(po.getUrl());
            pageUrlDao.entry(pageUrl);
        }
    }

    @Override
    public void addPersonalPerf(PersonalPerformance po) {
        personalPerformanceDao.entry(po);
        PersonalPerfKey personalPerfKey = personalPerfKeyDao.getByKey(po.getKey());
        if(personalPerfKey == null || personalPerfKey.getKey() == null || "".equals(personalPerfKey.getKey())){
            personalPerfKey = new PersonalPerfKey();
            personalPerfKey.setKey(po.getKey());
            personalPerfKeyDao.entry(personalPerfKey);
        }
    }

    @Override
    public PageAnalyticsDto queryLineChartData(int urlId, String startTimeStr, String endTimeStr, int unitTimeAboutMinutes) {
        PageAnalyticsDto pageAnalyticsDto = new PageAnalyticsDto();
        PageUrl pageUrl = pageUrlDao.getById(urlId);
        List<PagePerformance> pagePerformanceList = pagePerformanceDao.queryAboutAddTime(pageUrl.getUrl(), startTimeStr, endTimeStr);
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
                DataSourceForPoint dataSourceForPoint = new DataSourceForPoint();
                dataSourceForPoint.setPagePerformances(pagePerformances);
                dataSourceForPoint.setTime(pointStartTime);
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
        DataSourceForPoint dataSourceForPoint = new DataSourceForPoint();
        dataSourceForPoint.setPagePerformances(pagePerformances);
        dataSourceForPoint.setTime(pointStartTime);
        dataSourceForPoints.add(dataSourceForPoint);
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
            Double averagingPaint = dataPo.getPagePerformances().stream().filter(po -> po.getFirstPaint()>-1).collect(Collectors.averagingInt(PagePerformance::getFirstPaint));
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

    @Override
    public List<PageUrl> queryUrlList() {
        return pageUrlDao.queryAll();
    }

    @Override
    public PersonalAnalyticsDto queryPersonalLineChartData(int keyId, String startTimeStr, String endTimeStr, int unitTimeAboutMinutes) {
        PersonalAnalyticsDto personalAnalyticsDto = new PersonalAnalyticsDto();
        PersonalPerfKey personalPerfKey = personalPerfKeyDao.getById(keyId);
        List<PersonalPerformance> personalPerformancesList = personalPerformanceDao.queryAboutAddTime(personalPerfKey.getKey(), startTimeStr, endTimeStr);
        //按照时间把数据分段
        List<DataSourceForPoint> dataSourceForPoints = Lists.newArrayList();
        LocalDateTime pointStartTime = null;
        LocalDateTime pointEndTime = null;
        List<PersonalPerformance> personalPerformances = null;
        ZoneId currentZone = ZoneId.systemDefault();
        for (int i = 0; i < personalPerformancesList.size(); i++) {
            PersonalPerformance po = personalPerformancesList.get(i);
            Instant addTimeInstall = po.getAddTime().toInstant();
            LocalDateTime addTime = LocalDateTime.ofInstant(addTimeInstall, currentZone);
            if (pointStartTime == null) {
                pointStartTime = LocalDateTime.of(addTime.getYear(), addTime.getMonth(), addTime.getDayOfMonth(), addTime.getHour(), addTime.getMinute());
                pointEndTime = pointStartTime.plusMinutes(unitTimeAboutMinutes);
                personalPerformances = Lists.newArrayList(po);
            } else if (addTime.isBefore(pointEndTime)) {
                personalPerformances.add(po);
            } else {
                DataSourceForPoint dataSourceForPoint = new DataSourceForPoint();
                dataSourceForPoint.setPersonalPerformances(personalPerformances);
                dataSourceForPoint.setTime(pointStartTime);
                dataSourceForPoints.add(dataSourceForPoint);
                if (addTime.isAfter(pointEndTime.plusMinutes(unitTimeAboutMinutes))) {
                    pointStartTime = LocalDateTime.of(addTime.getYear(), addTime.getMonth(), addTime.getDayOfMonth(), addTime.getHour(), addTime.getMinute());
                    pointEndTime = pointStartTime.plusMinutes(unitTimeAboutMinutes);
                } else {
                    pointStartTime = pointEndTime;
                    pointEndTime = pointStartTime.plusMinutes(unitTimeAboutMinutes);
                }
                personalPerformances = Lists.newArrayList(po);
            }
        }
        DataSourceForPoint dataSourceForPoint = new DataSourceForPoint();
        dataSourceForPoint.setPersonalPerformances(personalPerformances);
        dataSourceForPoint.setTime(pointStartTime);
        dataSourceForPoints.add(dataSourceForPoint);
        //数据转化
        List<Point> timeList = Lists.newArrayList();
        for (int i = 0; i < dataSourceForPoints.size(); i++) {
            DataSourceForPoint dataPo = dataSourceForPoints.get(i);
            long xTime = dataPo.getTime().atZone(currentZone).toEpochSecond() * 1000;
            Point time = new Point();
            Double averagingTime = dataPo.getPersonalPerformances().stream().collect(Collectors.averagingInt(PersonalPerformance::getTime));
            time.setY(averagingTime.longValue());
            time.setX(xTime);
            timeList.add(time);

        }
        personalAnalyticsDto.setTimeList(timeList);
        return personalAnalyticsDto;
    }

    @Override
    public List<PersonalPerfKey> queryKeyList() {
        return personalPerfKeyDao.queryAll();
    }
}
