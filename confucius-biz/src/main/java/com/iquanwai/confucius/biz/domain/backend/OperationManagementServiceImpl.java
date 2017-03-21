package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.fragmentation.*;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationPractice;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPractice;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPracticeDiscuss;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.page.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;

/**
 * Created by justin on 17/3/16.
 */
@Service
public class OperationManagementServiceImpl implements OperationManagementService {
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private WarmupPracticeDao warmupPracticeDao;
    @Autowired
    private WarmupPracticeDiscussDao warmupPracticeDiscussDao;
    @Autowired
    private MessageService messageService;
    @Autowired
    private WarmupChoiceDao warmupChoiceDao;
    @Autowired
    private ProfileDao profileDao;
    //每个练习的精华上限
    private static final int HIGHLIGHT_LIMIT = 3;

    @Override
    public List<ApplicationSubmit> loadApplicationSubmit(Integer practiceId, Page page) {
        List<ApplicationSubmit> applicationSubmitList = applicationSubmitDao.getPracticeSubmit(practiceId, page);
        applicationSubmitList.stream().forEach(applicationSubmit -> {
            String openid = applicationSubmit.getOpenid();
            Profile profile = profileDao.queryByOpenId(openid);
            if (profile != null) {
                applicationSubmit.setUpName(profile.getNickname());
                applicationSubmit.setHeadPic(profile.getHeadimgurl());
            }
            applicationSubmit.setUpTime(DateUtils.parseDateToFormat5(applicationSubmit.getUpdateTime()));
        });

        return applicationSubmitList;
    }

    @Override
    public List<WarmupPractice> getLastTwoDayActivePractice() {
        List<Integer> warmupPracticeIds = warmupPracticeDiscussDao.loadHotWarmupPracticeDiscussLastNDay(2);
        return warmupPracticeDao.loadPractices(warmupPracticeIds);
    }

    @Override
    public WarmupPractice getWarmupPractice(Integer practiceId) {
        WarmupPractice warmupPractice = warmupPracticeDao.load(WarmupPractice.class, practiceId);
        warmupPractice.setDiscussList(warmupPracticeDiscussDao.loadDiscuss(practiceId));
        warmupPractice.setChoiceList(warmupChoiceDao.loadChoices(practiceId));
        return warmupPractice;
    }

    @Override
    public void discuss(String openid, Integer warmupPracticeId, String comment, Integer repliedId) {
        WarmupPracticeDiscuss warmupPracticeDiscuss = new WarmupPracticeDiscuss();
        warmupPracticeDiscuss.setWarmupPracticeId(warmupPracticeId);
        warmupPracticeDiscuss.setComment(comment);
        warmupPracticeDiscuss.setDel(0);
        warmupPracticeDiscuss.setOpenid(openid);
        if (repliedId != null) {
            WarmupPracticeDiscuss repliedDiscuss = warmupPracticeDiscussDao.load(WarmupPracticeDiscuss.class, repliedId);
            if (repliedDiscuss != null) {
                warmupPracticeDiscuss.setRepliedId(repliedId);
                warmupPracticeDiscuss.setRepliedComment(repliedDiscuss.getComment());
                warmupPracticeDiscuss.setRepliedOpenid(repliedDiscuss.getOpenid());
            }
        }
        int id = warmupPracticeDiscussDao.insert(warmupPracticeDiscuss);

        //发送回复通知
        if (repliedId != null && !openid.equals(warmupPracticeDiscuss.getRepliedOpenid())) {
            String url = "/rise/static/message/warmup/reply?commentId={0}&warmupPracticeId={1}";
            url = MessageFormat.format(url, id, warmupPracticeId);
            String message = "回复了我的理解训练问题";
            messageService.sendMessage(message, warmupPracticeDiscuss.getRepliedOpenid(),
                    openid, url);
        }
    }

    @Override
    public void highlightDiscuss(Integer discussId) {
        warmupPracticeDiscussDao.highlight(discussId);
    }

    @Override
    public void highlightApplicationSubmit(Integer practiceId, Integer submitId) {
        List<ApplicationSubmit> highlights = applicationSubmitDao.getHighlightSubmit(practiceId);

        //精华数超过上限时,把最先加精的作业去精
        if (highlights.size() >= HIGHLIGHT_LIMIT) {
            ApplicationSubmit oldest = null;
            for(ApplicationSubmit applicationSubmit:highlights){
                if(oldest==null){
                    oldest = applicationSubmit;
                }else{
                    if(applicationSubmit.getHighlightTime().before(oldest.getHighlightTime())){
                        oldest = applicationSubmit;
                    }
                }
            }

            if(oldest!=null) {
                applicationSubmitDao.unHighlight(oldest.getId());
            }
        }

        applicationSubmitDao.highlight(submitId);
    }
}
