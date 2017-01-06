package com.iquanwai.confucius.biz.domain.course.signup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.course.ClassDao;
import com.iquanwai.confucius.biz.dao.course.CourseDao;
import com.iquanwai.confucius.biz.dao.wx.CourseOrderDao;
import com.iquanwai.confucius.biz.po.Course;
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
    private CourseDao courseDao;
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
     * 每个人的报名班级
     * */
    private Map<String, CourseClass> signupMap = Maps.newConcurrentMap();

    public boolean isEntry(String openid, Integer courseId) {
        CourseClass classes = signupMap.get(openid);
        return classes!=null && CourseClass.getClassId(classes, courseId) != null;
    }

    @PostConstruct
    public void initClass(){
        List<QuanwaiClass> quanwaiClassList = classDao.openClass();
        List<Integer> openClass = Lists.newArrayList(); // 开放报名的班级id
        Map<Integer, AtomicInteger> maps = Maps.newHashMap();
        synchronized (lock) {
            remainingCount.clear();
            signupMap.clear();
            openClassList.clear();
            //初始化班级记录
            for (QuanwaiClass quanwaiClass : quanwaiClassList) {
                Integer classId = quanwaiClass.getId();
                Integer courseId = quanwaiClass.getCourseId();
                Course course = courseDao.load(Course.class, courseId);
                if(course!=null && course.getType()==Course.LONG_COURSE) {
                    openClass.add(classId);
                }
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
                    if(course!=null && course.getType()==Course.LONG_COURSE) {
                        remainingCount.put(classId, quanwaiClass.getLimit());
                    }else{
                        //短课程班级容量无限
                        remainingCount.put(classId, 1000000);
                    }
                    logger.info("init classId {} has {} quota total", classId, remainingCount.get(classId));
                }
            }
            //统计已付款和待付款的人数
            List<CourseOrder> courseOrders = courseOrderDao.loadClassOrder(openClass);
            for(CourseOrder courseOrder:courseOrders){
                String openid = courseOrder.getOpenid();
                Integer courseId = courseOrder.getCourseId();
                Integer classId = courseOrder.getClassId();
                //维护报名记录
                CourseClass courseClasses = signupMap.get(openid);
                if(courseClasses==null) {
                    courseClasses = new CourseClass();
                    CourseClass.addCourseEntry(courseClasses, courseId, classId);

                    //计算班级已付费和待付费的人数
                    AtomicInteger value = maps.get(classId);

                    if (value != null) {
                        value.incrementAndGet();
                    } else {
                        maps.put(classId, new AtomicInteger(1));
                    }
                }
            }
            //原有人数减去已付费和待付费人数=各班剩余名额
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
            //报名记录
            CourseClass entryRecord = signupMap.get(openid);
            if(entryRecord==null){
                entryRecord = new CourseClass();
                signupMap.put(openid, entryRecord);
            }
            Integer entryId = CourseClass.getClassId(entryRecord, courseId);
            //轮询所有班级，查看未报满的班级
            if(entryId==null){
                entryId = preEntry(entryRecord, courseId, openClass);
            }else{
                int remainingNumber = remainingCount.get(entryId);
                // 如果名额已满,不能再报当前班级,需重新查找未报满的班级
                if(remainingNumber<=0){
                    CourseClass.removeCourse(entryRecord, courseId);
                    entryId = preEntry(entryRecord, courseId, openClass);
                }
            }

            //找到未报满的班级，完成预报名
            if(entryId!=null) {
                return new ImmutablePair<>(1, entryId);
            }
        }
        return new ImmutablePair(-1, 0);
    }

    private Integer preEntry(CourseClass courseClasses, Integer courseId, List<Integer> openClass) {
        for(Integer classId:openClass){
            int remainingNumber = remainingCount.get(classId);
            if(remainingNumber>0){
                //人数-1，记录班级id，标记分配进入某班
                remainingNumber--;
                remainingCount.put(classId, remainingNumber);
                CourseClass.addCourseEntry(courseClasses, courseId, classId);
                return classId;
            }
        }
        //全部报满时,返回null
        return null;
    }

    public void quitClass(String openid, Integer courseId, Integer classId) {
        CourseClass classes = signupMap.get(openid);
        Integer realClassId = CourseClass.getClassId(classes, courseId);
        //订单的classId和课程classId相同时才退名额
        if (classId.equals(realClassId)) {
            synchronized (lock) {
                Integer remaining = remainingCount.get(classId);
                remainingCount.put(classId, remaining+1);
                logger.info("init classId {} has {} quota left", classId, remaining+1);
            }
        }
    }

    @Data
    private static class CourseClass{
        private Map<Integer,Integer> classMap = Maps.newHashMap();

        //添加课程报名记录
        public static void addCourseEntry(CourseClass classes, Integer courseId, Integer classId) {
            classes.getClassMap().put(courseId, classId);
        }

        //获取课程班级id
        public static Integer getClassId(CourseClass classes, Integer courseId) {
            if(classes==null){
                return null;
            }
            return classes.getClassMap().get(courseId);
        }

        //删除报名的课程
        public static void removeCourse(CourseClass classes, Integer courseId) {
            classes.getClassMap().remove(courseId);
        }

    }
}
