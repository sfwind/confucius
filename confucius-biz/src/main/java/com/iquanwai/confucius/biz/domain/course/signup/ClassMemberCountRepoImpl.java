package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.course.ClassDao;
import com.iquanwai.confucius.biz.dao.wx.CourseOrderDao;
import com.iquanwai.confucius.biz.po.CourseOrder;
import com.iquanwai.confucius.biz.po.QuanwaiClass;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by justin on 16/9/30.
 */
@Repository
public class ClassMemberCountRepoImpl implements ClassMemberCountRepo {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ClassDao classDao;
    @Autowired
    private CourseOrderDao courseOrderDao;

    private final Object lock = new Object();

    /**
     * 每个课程的对应开班id
     * */
    private Map<Integer, List<Integer>> openClassList = Maps.newConcurrentMap();
    /**
     * 每个班级的剩余人数
     * */
    private Map<Integer, Integer> remainingCount = Maps.newConcurrentMap();

    /**
     * 每个人的报名班级id
     * */
    private Map<String, List<CourseClass>> signupMap = Maps.newConcurrentMap();

    public boolean isEntry(String openid, Integer courseId) {
        List<CourseClass> classes = signupMap.get(openid);
        return classes!=null && CourseClass.getCourse(classes, courseId)!=null;
    }

    @PostConstruct
    public void initClass(){
        List<QuanwaiClass> quanwaiClassList = classDao.openClass();
        List<Integer> openClass = Lists.newArrayList(); // 开放报名的班级id
        Map<Integer, AtomicInteger> maps = Maps.newHashMap();
        synchronized (lock) {
            remainingCount.clear();
            signupMap.clear();
            //初始化班级记录
            for (QuanwaiClass quanwaiClass : quanwaiClassList) {
                Integer classId = quanwaiClass.getId();
                openClass.add(classId);
                Integer remaining = remainingCount.get(classId);
                if (remaining == null) {
                    List<Integer> classList = openClassList.get(quanwaiClass.getCourseId());
                    if(classList==null){
                        classList = Lists.newArrayList();
                        openClassList.put(quanwaiClass.getCourseId(), classList);
                    }
                    if(!classList.contains(classId)){
                        classList.add(classId);
                    }
                    remainingCount.put(classId, quanwaiClass.getLimit());
                    logger.info("init classId {} has {} quota total", classId, quanwaiClass.getLimit());
                }
            }
            //统计已付款和待付款的人数
            List<CourseOrder> courseOrders = courseOrderDao.loadClassOrder(openClass);
            for(CourseOrder courseOrder:courseOrders){
                if(signupMap.get(courseOrder.getOpenid())!=null){
                    continue;
                }
                Integer classId = courseOrder.getClassId();
                AtomicInteger value = maps.get(classId);

                if (value != null) {
                    value.incrementAndGet();
                } else {
                    maps.put(classId, new AtomicInteger(1));
                }
                String openid = courseOrder.getOpenid();
                List<CourseClass> courseClasses = signupMap.get(openid);
                if(courseClasses==null){
                    courseClasses = Lists.newArrayList();
                    signupMap.put(openid, courseClasses);
                }
                CourseClass.addCourse(courseClasses, courseOrder.getCourseId(), classId);
            }
            //原有人数减去已付费和待付费人数
            for (Map.Entry<Integer, AtomicInteger> entry : maps.entrySet()) {
                Integer classId = entry.getKey();
                Integer remaining = remainingCount.get(classId);
                int rest = (remaining - maps.get(classId).get()) < 0 ? 0 : (remaining - maps.get(classId).get());
                remainingCount.put(classId, rest);
                logger.info("init classId {} has {} quota left", classId, rest);
            }
        }
        logger.info("init class sign up number complete");
    }

    public Pair<Integer, Integer> prepareSignup(String openid, Integer courseId) {
        //计算剩余人数
        synchronized (lock) {
            List<Integer> openClass = openClassList.get(courseId);
            if(CollectionUtils.isEmpty(openClass)){
                return new ImmutablePair(-2, 0);
            }
            int remain = 0;
            boolean isEntry = false; //是否已经进入某班
            List<CourseClass> classes = signupMap.get(openid);
            if(classes==null){
                classes = Lists.newArrayList();
                signupMap.put(openid, classes);
            }
            CourseClass courseClass = CourseClass.getCourse(classes, courseId);
            Integer entryId = courseClass!=null?courseClass.getClassId():null;
            //轮询所有班级，查看未报满的
            if(entryId==null){
                for(Integer classId:openClass){
                    int remainingNumber = remainingCount.get(classId);
                    if(remainingNumber<=0){
                        continue;
                    }else{
                        if(!isEntry) {
                            //人数-1，记录班级id，标记分配进入某班
                            remainingNumber--;
                            remainingCount.put(classId, remainingNumber);
                            List<CourseClass> courseClasses = signupMap.get(openid);
                            if(courseClasses==null){
                                courseClasses = Lists.newArrayList();
                                signupMap.put(openid, courseClasses);
                            }
                            CourseClass.addCourse(courseClasses, courseId, classId);
                            entryId = classId;
                            isEntry = true;
                        }
                    }
                    remain = remain+remainingNumber;
                }
            }else{
                isEntry = true;
            }

            //找到未报满的班级，完成预报名
            if(isEntry) {
                return new ImmutablePair<>(remain, entryId);
            }
        }
        return new ImmutablePair(-1, 0);
    }

    public void quitClass(String openid, Integer courseId) {
        List<CourseClass> classes = signupMap.get(openid);
        Integer classId = CourseClass.removeCourse(classes, courseId).getClassId();
        QuanwaiClass quanwaiClass = classDao.load(QuanwaiClass.class, classId);
        if(quanwaiClass==null){
            return;
        }
        synchronized (lock) {
            Integer remaining = remainingCount.get(quanwaiClass.getId());
            remainingCount.put(classId, remaining+1);
        }
    }

    @Data
    private static class CourseClass{
        private Integer courseId;
        private Integer classId;

        public static CourseClass build(Integer courseId, Integer classId){
            CourseClass courseClass = new CourseClass();
            courseClass.setClassId(classId);
            courseClass.setCourseId(courseId);

            return courseClass;
        }

        public static void addCourse(List<CourseClass> classes, Integer courseId, Integer classId) {
            classes.add(build(courseId, classId));
        }

        public static CourseClass getCourse(List<CourseClass> classes, Integer courseId) {
            Optional<CourseClass> optional = classes.stream().filter(courseClass -> courseClass.getCourseId().equals(courseId))
                    .findFirst();
            return optional.isPresent()?optional.get():null;
        }

        public static CourseClass removeCourse(List<CourseClass> classes, Integer courseId) {
            Optional<CourseClass> optional = classes.stream().filter(courseClass -> courseClass.getCourseId().equals(courseId))
                    .findFirst();

            CourseClass result = optional.isPresent()?optional.get():null;
            if(result!=null) {
                classes.remove(result);
            }

            return result;
        }
    }
}
