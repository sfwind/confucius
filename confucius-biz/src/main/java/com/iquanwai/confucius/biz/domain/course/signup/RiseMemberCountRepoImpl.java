package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.dao.RedisUtil;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseOrderDao;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.RiseOrder;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.Constants;
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
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RiseOrderDao riseOrderDao;
    @Autowired
    private ProfileDao profileDao;

    //精英用户余额
    private final static String REMAIN_NUMBER_KEY = "remain:elite:set";
    //精英用户余额锁
    private final static String REMAIN_NUMBER_LOCK_KEY = "lock:elite:number:set";

    @PostConstruct
    @Override
    public void init() {
        logger.info("初始化圈外会员报名参数");

        redisUtil.lock(REMAIN_NUMBER_LOCK_KEY, (lock) -> {
            // 查询当前已报名的人数和待付款的人数
            AtomicInteger remainCount = new AtomicInteger(ConfigUtils.riseMemberTotal());
            // 未付款+未过期=所占名额
            // 未付款
//          Integer holderCount = riseOrderDao.loadHolderCount();
            // 未关闭的订单中，如果用户已经是rise会员，则这个未关闭订单不要再占据一个名额
            List<RiseOrder> holderList = riseOrderDao.loadActiveOrder();
            long holderCount = holderList.stream().filter(item -> {
                Profile profile = profileDao.queryByOpenId(item.getOpenid());
                return profile.getRiseMember() == Constants.RISE_MEMBER.FREE;
            }).map(RiseOrder::getOpenid).distinct().count();
            // 未过期
            Integer nowCount = profileDao.riseMemberCount();
            Long total = nowCount+holderCount;
            logger.info("当前圈外会员:{},待付费人数:{},总名额:{},剩余名额:{}", nowCount, holderCount, total,
                    remainCount.get() - total);
            Integer remain = remainCount.addAndGet(-total.intValue());
            // 剩余人数
            if (remain < 0) {
                remainCount.set(0);
            }
            redisUtil.set(REMAIN_NUMBER_KEY, remainCount.get(), 60*60*24*30L);
        });
    }

    @Override
    public void reload(){
        this.init();
    }

    @Override
    public Pair<Integer, String> prepareSignup(Integer profileId) {
        return this.prepareSignup(profileId, true);
    }

    @Override
    public Pair<Integer,String> prepareSignup(Integer profileId, Boolean hold){
        Profile profile = profileDao.load(Profile.class, profileId);
        if(profile.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP){
            // 已经报名
            return new MutablePair<>(-3,"您已是圈外会员");
        } else if(profile.getRiseMember() == Constants.RISE_MEMBER.TRAIN_CAMP){
            return new MutablePair<>(-3, "您已是小课训练营会员");
        } else {
            // 未报名,查看是否有未关闭的订单
            Integer counts = riseOrderDao.userNotCloseOrder(profileId);
            if(counts>0){
                // 如果有未关闭的订单，则即使未再报名期间之内应该也可以报名
                return new MutablePair<>(1,"ok");
            } else {
                // 如果不是在报名时间内，则禁止报名
                if(ConfigUtils.getRisePayStopTime().before(new Date())){
                    return new MutablePair<>(-4, "Hi，谢谢你关注【圈外同学】!\n不过...本次报名已达到限额了\n记得及时关注下期开放通知哦");
                }
            }
        }
        // TODO 不限制报名人数
//        Integer remainCount = getRemindingCount();
//        if(remainCount <= 0){
//            return new MutablePair<>(-1,"报名人数已满");
//        } else {
//            // 是否要占
//            if(hold){
//                redisUtil.lock(REMAIN_NUMBER_LOCK_KEY, (lock) -> {
//                    Integer remainCountFinal = getRemindingCount();
//                    logger.info("剩余精英用户:{}", remainCountFinal);
//                    redisUtil.set(REMAIN_NUMBER_KEY, remainCountFinal-1, 60*60*24*30L);
//                });
//            }
            return new MutablePair<>(1,"ok");
//        }
    }

    @Override
    public void quitSignup(Integer profileId, Integer memberTypeId) {
        redisUtil.lock(REMAIN_NUMBER_LOCK_KEY, (lock) -> {
            Integer remainCount = getRemindingCount();
            logger.info("剩余精英用户:{}", remainCount);
            redisUtil.set(REMAIN_NUMBER_KEY, remainCount+1, 60*60*24*30L);
        });
    }

    @Override
    public Integer getRemindingCount(){
        return Integer.parseInt(redisUtil.get(REMAIN_NUMBER_KEY));
    }


}

