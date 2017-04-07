package com.iquanwai.confucius.biz.domain.course.signup;

import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseOrderDao;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nethunder on 2017/4/7.
 */
@Repository
public class RiseMemberCountRepoImpl implements RiseMemberCountRepo {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    // 剩余的人数
    private AtomicInteger remainCount;



    @Autowired
    private RiseOrderDao riseOrderDao;
    @Autowired
    private ProfileDao profileDao;


//    private Map<String,List<Integer>>

    @PostConstruct
    @Override
    public synchronized void init() {
        logger.info("初始化rise会员报名参数");
        // 查询当前已报名的人数和待付款的人数
        remainCount = new AtomicInteger(500);
        Integer integer = riseOrderDao.loadHolderCount();
        Integer remain = remainCount.addAndGet(-integer);
        // 剩余人数
        if (remain < 0) {
            remainCount.set(0);
        }
    }

    @Override
    public Pair<Integer, String> prepareSignup(String openId) {
        Profile profile = profileDao.queryByOpenId(openId);
        if(profile.getRiseMeber()){
            // 已经报名
            return new MutablePair<>(-1,"您已是RISER");
        }
        return null;
    }

    @Override
    public void quitSignup(String openId, Integer memberTypeId) {

    }


}

