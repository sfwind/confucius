package com.iquanwai.confucius.biz.domain.fragmentation.practice;

import com.iquanwai.confucius.biz.dao.fragmentation.WarmupPracticeDiscussDao;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPracticeDiscuss;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussServiceImpl implements DiscussService{
    @Autowired
    private WarmupPracticeDiscussDao warmupPracticeDiscussDao;

    @Override
    public List<WarmupPracticeDiscuss> loadTodayDiscuss() {
        return warmupPracticeDiscussDao.loadTodayDiscuss();
    }

    @Override
    public List<WarmupPracticeDiscuss> loadByReplys(List<Integer> replys) {
        return warmupPracticeDiscussDao.loadByRelays(replys);
    }

    @Override
    public List<WarmupPracticeDiscuss> loadTargetDiscuss(Integer practiceId, String currentDate) {
        return warmupPracticeDiscussDao.loadTargetDiscuss(practiceId,currentDate);
    }
}
