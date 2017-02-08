package com.iquanwai.confucius.biz.domain.customer;

import com.iquanwai.confucius.biz.dao.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.wx.FollowUserDao;
import com.iquanwai.confucius.biz.po.Account;
import com.iquanwai.confucius.biz.po.customer.Profile;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Created by nethunder on 2017/2/8.
 */
@Service
public class ProfileServiceImpl implements ProfileService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private FollowUserDao followUserDao;

    @Override
    public Profile getProfile(String openId) {
        Profile profile =  profileDao.queryByOpenId(openId);
        if(profile==null){
            // 查询一下account
            Account account = followUserDao.queryByOpenid(openId);
            if(account!=null){
                profile = new Profile();
                ModelMapper modelMapper = new ModelMapper();
                modelMapper.map(account, profile);
                profileDao.insertProfile(profile);
            }
        }
        return profile;
    }

    @Override
    public void submitPersonalCenterProfile(Profile profile) {
        Assert.notNull(profile.getOpenid(), "openID不能为空");
        profileDao.submitPersonalCenterProfile(profile);
    }

}
