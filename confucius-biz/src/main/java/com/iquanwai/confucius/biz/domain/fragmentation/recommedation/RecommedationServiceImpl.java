package com.iquanwai.confucius.biz.domain.fragmentation.recommedation;

import com.iquanwai.confucius.biz.dao.fragmentation.UserRecommedationDao;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecommedationServiceImpl implements RecommedationService {

    @Autowired
    private UserRecommedationDao userRecommedationDao;
    @Autowired
    private AccountService accountService;

    /**
     * 添加用户推荐
     * @param openId
     * @param riseId
     * @return
     */
    @Override
    public int addUserRecommedation(String openId, String riseId) {

        Profile profile = accountService.getProfileByRiseId(riseId);

        if (profile != null) {
            userRecommedationDao.insert(profile.getId(), openId);
        }

        return 0;
    }
}
