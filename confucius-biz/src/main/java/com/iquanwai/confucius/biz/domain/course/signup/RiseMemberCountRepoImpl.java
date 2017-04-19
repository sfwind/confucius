package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseOrderDao;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.RiseOrder;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nethunder on 2017/4/7.
 */
@Repository
public class RiseMemberCountRepoImpl implements RiseMemberCountRepo {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    // 剩余的人数
    private AtomicInteger remainCount;
    public static final Object lock = new Object();


    @Autowired
    private RiseOrderDao riseOrderDao;
    @Autowired
    private ProfileDao profileDao;



    @PostConstruct
    @Override
    public synchronized void init() {
        logger.info("初始化rise会员报名参数");
        // 查询当前已报名的人数和待付款的人数
        remainCount = new AtomicInteger(ConfigUtils.riseMemberTotal());
        // 未付款+未过期=所占名额
        // 未付款
//        Integer holderCount = riseOrderDao.loadHolderCount();
        // 未关闭的订单中，如果用户已经是rise会员，则这个未关闭订单不要再占据一个名额
        List<RiseOrder> holderList = riseOrderDao.loadActiveOrder();
        long holderCount = holderList.stream().filter(item -> {
            Profile profile = profileDao.queryByOpenId(item.getOpenid());
            return !profile.getRiseMember();
        }).map(RiseOrder::getOpenid).distinct().count();
        // 未过期
        Integer nowCount = profileDao.riseMemberCount();
        Long total = nowCount+holderCount;
        logger.info("当前RISE会员:{},待付费人数:{},总名额:{},剩余名额:{}", nowCount, holderCount, total, remainCount.get() - total);
        Integer remain = remainCount.addAndGet(-total.intValue());
        // 剩余人数
        if (remain < 0) {
            remainCount.set(0);
        }
    }

    @Override
    public void reload(){
        this.init();
    }

    @Override
    public Pair<Integer, String> prepareSignup(String openId) {
        return this.prepareSignup(openId, true);
    }

    @Override
    public Pair<Integer,String> prepareSignup(String openId, Boolean hold){
        Profile profile = profileDao.queryByOpenId(openId);
        if(profile.getRiseMember()){
            // 已经报名
            return new MutablePair<>(-3,"您已是RISER");
        } else {
            // 未报名,查看是否有未关闭的订单
            Integer counts = riseOrderDao.userNotCloseOrder(openId);
            if(counts>0){
                // 如果有未关闭的订单，则即使未再报名期间之内应该也可以报名
                return new MutablePair<>(1,"ok");
            } else {
                // 如果不是在报名时间内，则禁止报名
                if(ConfigUtils.getRisePayStopTime().before(new Date())){
                    return new MutablePair<>(-4, "Hi，谢谢你关注RISE!\n不过...本次报名已达到限额了\n记得及时关注下期开放通知哦");
                }
            }
        }
        synchronized (lock){
            if(remainCount.get() <= 0){
                return new MutablePair<>(-1,"报名人数已满");
            } else {
                // 是否要占
                if(hold){
                    remainCount.decrementAndGet();
                }
                return new MutablePair<>(1,"ok");
            }
        }
    }

    @Override
    public void quitSignup(String openId, Integer memberTypeId) {
        synchronized (lock){
            // 退还名额
            remainCount.incrementAndGet();
        }
    }

    @Override
    public Integer getRemindingCount(){
        return remainCount.get();
    }


}

