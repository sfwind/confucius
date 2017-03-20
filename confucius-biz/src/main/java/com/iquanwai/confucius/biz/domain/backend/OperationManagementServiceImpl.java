package com.iquanwai.confucius.biz.domain.backend;

import com.iquanwai.confucius.biz.dao.fragmentation.ApplicationSubmitDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ChoiceDao;
import com.iquanwai.confucius.biz.dao.fragmentation.WarmupPracticeDao;
import com.iquanwai.confucius.biz.dao.fragmentation.WarmupPracticeDiscussDao;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.po.fragmentation.ApplicationSubmit;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPractice;
import com.iquanwai.confucius.biz.po.fragmentation.WarmupPracticeDiscuss;
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
    private WarmupPracticeDao warmupPracticeDao;
    @Autowired
    private WarmupPracticeDiscussDao warmupPracticeDiscussDao;
    @Autowired
    private MessageService messageService;
    @Autowired
    private ChoiceDao choiceDao;

    @Override
    public List<ApplicationSubmit> loadApplicationSubmit(Integer practiceId, Page page) {
        return applicationSubmitDao.getPracticeSubmit(practiceId, page);
    }

    @Override
    public List<WarmupPractice> getLastTwoDayActivePractice() {
        List<Integer> warmupPracticeIds = warmupPracticeDiscussDao.loadHotWarmupPracticeDiscussLastNDay(2);
        return warmupPracticeDao.loadPractices(warmupPracticeIds);
    }

    @Override
    public WarmupPractice getWarmupPractice(Integer practiceId){
        WarmupPractice warmupPractice = warmupPracticeDao.load(WarmupPractice.class, practiceId);
        warmupPractice.setDiscussList(warmupPracticeDiscussDao.loadDiscuss(practiceId));
        warmupPractice.setChoiceList(choiceDao.loadChoices(practiceId));
        return warmupPractice;
    }

    @Override
    public void discuss(String openid, Integer warmupPracticeId, String comment, Integer repliedId) {
        WarmupPracticeDiscuss warmupPracticeDiscuss = new WarmupPracticeDiscuss();
        warmupPracticeDiscuss.setWarmupPracticeId(warmupPracticeId);
        warmupPracticeDiscuss.setComment(comment);
        warmupPracticeDiscuss.setDel(0);
        warmupPracticeDiscuss.setOpenid(openid);
        if(repliedId!=null) {
            WarmupPracticeDiscuss repliedDiscuss = warmupPracticeDiscussDao.load(WarmupPracticeDiscuss.class, repliedId);
            if(repliedDiscuss!=null){
                warmupPracticeDiscuss.setRepliedId(repliedId);
                warmupPracticeDiscuss.setRepliedComment(repliedDiscuss.getComment());
                warmupPracticeDiscuss.setRepliedOpenid(repliedDiscuss.getOpenid());
            }
        }
        int id = warmupPracticeDiscussDao.insert(warmupPracticeDiscuss);

        //发送回复通知
        if(repliedId!=null && !openid.equals(warmupPracticeDiscuss.getRepliedOpenid())) {
            String url = "/rise/static/message/warmup/reply?commentId={0}&warmupPracticeId={1}";
            url = MessageFormat.format(url, id, warmupPracticeId);
            String message = "回复了我的理解训练问题";
            messageService.sendMessage(message, warmupPracticeDiscuss.getRepliedOpenid(),
                    openid, url);
        }
    }

    @Override
    public void highlight(Integer discussId) {
        warmupPracticeDiscussDao.highlight(discussId);
    }
}
