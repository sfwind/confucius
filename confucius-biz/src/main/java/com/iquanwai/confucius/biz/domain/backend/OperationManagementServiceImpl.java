package com.iquanwai.confucius.biz.domain.backend;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.fragmentation.ApplicationSubmitDao;
import com.iquanwai.confucius.biz.dao.fragmentation.WarmupChoiceDao;
import com.iquanwai.confucius.biz.dao.fragmentation.WarmupPracticeDao;
import com.iquanwai.confucius.biz.dao.fragmentation.WarmupPracticeDiscussDao;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
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

    private static final String SYSTEM_MESSAGE = "AUTO";

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
        List<WarmupPracticeDiscuss> warmupPracticeDiscusses = warmupPracticeDiscussDao.loadDiscuss(practiceId);
        warmupPracticeDiscusses.stream().forEach(discuss ->{
            String openid = discuss.getOpenid();
            Profile profile = profileDao.queryByOpenId(openid);
            if(profile!=null){
                discuss.setAvatar(profile.getHeadimgurl());
                discuss.setName(profile.getNickname());
            }
        });
        warmupPractice.setDiscussList(warmupPracticeDiscusses);
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
        Integer id = warmupPracticeDiscussDao.insert(warmupPracticeDiscuss);

        //发送回复通知
        if (repliedId != null && !openid.equals(warmupPracticeDiscuss.getRepliedOpenid())) {
            String url = "/rise/static/message/warmup/reply?commentId={0}&warmupPracticeId={1}";
            url = MessageFormat.format(url, id.toString(), warmupPracticeId.toString());
            String message = "回复了我的理解训练问题";
            messageService.sendMessage(message, warmupPracticeDiscuss.getRepliedOpenid(),
                    openid, url);
        }
    }

    @Override
    public void highlightDiscuss(Integer discussId) {
        warmupPracticeDiscussDao.highlight(discussId);

        WarmupPracticeDiscuss warmupPracticeDiscuss = warmupPracticeDiscussDao.load(WarmupPracticeDiscuss.class, discussId);

        if(warmupPracticeDiscuss!=null){
            Integer practiceId = warmupPracticeDiscuss.getWarmupPracticeId();

            //通知被认证者
            String highlightOne = warmupPracticeDiscuss.getOpenid();

            String url = "/rise/static/message/warmup/reply?commentId={0}&warmupPracticeId={1}";
            url = MessageFormat.format(url, discussId.toString(), practiceId.toString());
            String message = "你对一个理解训练题的解答很棒，并得到了官方的认证，点击看看吧";
            messageService.sendMessage(message, highlightOne,
                    SYSTEM_MESSAGE, url);

            //通知所有参与过讨论的用户
            List<WarmupPracticeDiscuss> warmupPracticeDiscussList = warmupPracticeDiscussDao.loadDiscuss(practiceId);

            List<String> participants = Lists.newArrayList();
            warmupPracticeDiscussList.stream().forEach(discuss -> {
                if(!participants.contains(discuss.getOpenid())){
                    participants.add(discuss.getOpenid());
                }
            });

            participants.stream().filter(participant -> !participant.equals(highlightOne)).forEach(participant -> {
                String url2 = "/rise/static/message/warmup/reply?commentId={0}&warmupPracticeId={1}";
                url2 = MessageFormat.format(url2, discussId.toString(), practiceId.toString());
                String message2 = "你关注的理解训练题，有一个解答被官方认证了，点击看看吧";
                messageService.sendMessage(message2, participant,
                        SYSTEM_MESSAGE, url2);
            });
        }
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