package com.iquanwai.confucius.biz.domain.backend;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.dao.fragmentation.*;
import com.iquanwai.confucius.biz.domain.message.MessageService;
import com.iquanwai.confucius.biz.domain.weixin.account.AccountService;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.Role;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.po.fragmentation.*;
import com.iquanwai.confucius.biz.util.Constants;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.biz.util.page.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private AccountService accountService;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private ProblemDao problemDao;
    @Autowired
    private KnowledgeDao knowledgeDao;
    @Autowired
    private ProblemScheduleDao problemScheduleDao;

    //每个练习的精华上限
    private static final int HIGHLIGHT_LIMIT = 10;

    private static final String SYSTEM_MESSAGE = "AUTO";

    @Override
    public List<ApplicationSubmit> loadApplicationSubmit(Integer practiceId, Page page) {
        List<ApplicationSubmit> applicationSubmitList = applicationSubmitDao.getPracticeSubmit(practiceId, page);
        applicationSubmitList.stream().forEach(applicationSubmit -> {
            Integer profileId = applicationSubmit.getProfileId();
            Profile profile = accountService.getProfile(profileId);
            if(profile != null) {
                applicationSubmit.setUpName(profile.getNickname());
                applicationSubmit.setHeadPic(profile.getHeadimgurl());
            }
            applicationSubmit.setUpTime(DateUtils.parseDateToFormat5(applicationSubmit.getPublishTime()));
        });

        return applicationSubmitList;
    }

    @Override
    public List<WarmupPractice> getLastSixtyDayActivePractice(Page page) {
        List<Integer> warmupPracticeIds = warmupPracticeDiscussDao.loadHotWarmupPracticeDiscussLastNDay(60, page);
        List<WarmupPractice> warmupPractices = warmupPracticeDao.loadPractices(warmupPracticeIds);

        warmupPractices.forEach(warmupPractice -> {
            Problem problem = problemDao.load(Problem.class, warmupPractice.getProblemId());
            if(problem != null) {
                warmupPractice.setProblemName(problem.getProblem());
            }
        });
        return warmupPractices;
    }

    @Override
    public WarmupPractice getWarmupPractice(Integer practiceId) {
        WarmupPractice warmupPractice = warmupPracticeDao.load(WarmupPractice.class, practiceId);
        List<WarmupPracticeDiscuss> warmupPracticeDiscusses = warmupPracticeDiscussDao.loadDiscuss(practiceId);

        warmupPracticeDiscusses.stream().forEach(discuss -> {
            Integer profileId = discuss.getProfileId();
            Profile profile = accountService.getProfile(profileId);
            if(profile != null) {
                discuss.setAvatar(profile.getHeadimgurl());
                discuss.setName(profile.getNickname());
            }
            discuss.setDiscussTime(DateUtils.parseDateToString(discuss.getAddTime()));
        });
        warmupPractice.setDiscussList(warmupPracticeDiscusses);
        warmupPractice.setChoiceList(warmupChoiceDao.loadChoices(practiceId));
        return warmupPractice;
    }

    @Override
    public WarmupPractice getTargetPractice(Integer practiceId,String currentDate) {
        WarmupPractice warmupPractice = warmupPracticeDao.load(WarmupPractice.class, practiceId);
        List<WarmupPracticeDiscuss> warmupPracticeDiscusses = warmupPracticeDiscussDao.loadTargetDiscuss(practiceId,currentDate);

        //TODO:获得某个评论的从头开始的回复
        List<Integer> origins = warmupPracticeDiscusses.stream().map(WarmupPracticeDiscuss::getOriginDiscussId).distinct().collect(Collectors.toList());

        List<WarmupPracticeDiscuss> originDiscusses = warmupPracticeDiscussDao.loadDiscussByOrigins(origins);
        warmupPracticeDiscusses.forEach(discuss->{
            Integer profileId = discuss.getProfileId();
            Profile profile = accountService.getProfile(profileId);
            if(profile != null) {
                discuss.setAvatar(profile.getHeadimgurl());
                discuss.setName(profile.getNickname());
            }
            discuss.setDiscussTime(DateUtils.parseDateToString(discuss.getAddTime()));
            List<AbstractComment> discusses = originDiscusses.stream().filter(warmupPracticeDiscuss -> warmupPracticeDiscuss.getOriginDiscussId().equals(discuss.getOriginDiscussId()) && warmupPracticeDiscuss.getId()<=discuss.getId()).collect(Collectors.toList());
            discuss.setDiscusses(discusses);
        });
        warmupPractice.setChoiceList(warmupChoiceDao.loadChoices(practiceId));
        return warmupPractice;
    }

    @Override
    public void discuss(Integer profileId, Integer warmupPracticeId, String comment, Integer repliedId) {
        WarmupPracticeDiscuss warmupPracticeDiscuss = new WarmupPracticeDiscuss();
        warmupPracticeDiscuss.setWarmupPracticeId(warmupPracticeId);
        warmupPracticeDiscuss.setComment(comment);
        warmupPracticeDiscuss.setDel(0);
        warmupPracticeDiscuss.setPriority(0);
        warmupPracticeDiscuss.setProfileId(profileId);
        if(repliedId != null) {
            WarmupPracticeDiscuss repliedDiscuss = warmupPracticeDiscussDao.load(WarmupPracticeDiscuss.class, repliedId);
            if(repliedDiscuss != null) {
                warmupPracticeDiscuss.setRepliedId(repliedId);
                warmupPracticeDiscuss.setRepliedComment(repliedDiscuss.getComment());
                warmupPracticeDiscuss.setRepliedProfileId(repliedDiscuss.getProfileId());
                warmupPracticeDiscuss.setOriginDiscussId(repliedDiscuss.getOriginDiscussId());
            }
        }
        Integer id = warmupPracticeDiscussDao.insert(warmupPracticeDiscuss);

        //发送回复通知
        if(repliedId != null && !profileId.equals(warmupPracticeDiscuss.getRepliedProfileId())) {
            String url = "/rise/static/message/warmup/reply?commentId={0}&warmupPracticeId={1}";
            url = MessageFormat.format(url, Objects.toString(id), Objects.toString(warmupPracticeId));
            String message = "回复了我的选择题问题";
            messageService.sendMessage(message, Objects.toString(warmupPracticeDiscuss.getRepliedProfileId()),
                    Objects.toString(profileId), url);
        }
    }

    @Override
    public void highlightDiscuss(Integer discussId) {
        warmupPracticeDiscussDao.highlight(discussId);

        WarmupPracticeDiscuss warmupPracticeDiscuss = warmupPracticeDiscussDao.load(WarmupPracticeDiscuss.class, discussId);

        if(warmupPracticeDiscuss != null) {
            Integer practiceId = warmupPracticeDiscuss.getWarmupPracticeId();

            //通知被认证者
            Integer highlightOne = warmupPracticeDiscuss.getProfileId();

            String url = "/rise/static/message/warmup/reply?commentId={0}&warmupPracticeId={1}";
            url = MessageFormat.format(url, Objects.toString(discussId), Objects.toString(practiceId));
            String message = "你对一个选择题的解答很棒，并得到了官方的认证，点击看看吧";
            messageService.sendMessage(message, Objects.toString(highlightOne),
                    SYSTEM_MESSAGE, url);

            //通知所有参与过讨论的用户
            List<WarmupPracticeDiscuss> warmupPracticeDiscussList = warmupPracticeDiscussDao.loadDiscuss(practiceId);

            List<Integer> participants = Lists.newArrayList();
            warmupPracticeDiscussList.stream().forEach(discuss -> {
                if(!participants.contains(discuss.getProfileId())) {
                    participants.add(discuss.getProfileId());
                }
            });

            participants.stream().filter(participant -> !participant.equals(highlightOne)).forEach(participant -> {
                String url2 = "/rise/static/message/warmup/reply?commentId={0}&warmupPracticeId={1}";
                url2 = MessageFormat.format(url2, Objects.toString(discussId), Objects.toString(practiceId));
                String message2 = "你关注的选择题，有一个解答被官方认证了，点击看看吧";
                messageService.sendMessage(message2, Objects.toString(participant),
                        SYSTEM_MESSAGE, url2);
            });
        }
    }

    @Override
    public void highlightApplicationSubmit(Integer practiceId, Integer submitId) {
        List<ApplicationSubmit> highlights = applicationSubmitDao.getHighlightSubmit(practiceId);

        //精华数超过上限时,把最先加精的作业去精
        if(highlights.size() >= HIGHLIGHT_LIMIT) {
            ApplicationSubmit oldest = null;
            for(ApplicationSubmit applicationSubmit : highlights) {
                if(oldest == null) {
                    oldest = applicationSubmit;
                } else {
                    if(applicationSubmit.getHighlightTime().before(oldest.getHighlightTime())) {
                        oldest = applicationSubmit;
                    }
                }
            }

            if(oldest != null) {
                applicationSubmitDao.unHighlight(oldest.getId());
            }
        }

        applicationSubmitDao.highlight(submitId);
    }

    @Override
    public boolean isComment(Integer submitId, Integer commentProfileId) {
        Comment comment = commentDao.loadComment(Constants.CommentModule.APPLICATION, submitId, commentProfileId);

        return comment != null;
    }

    @Override
    public List<WarmupPractice> getPracticeByProblemId(Integer problemId) {
        return warmupPracticeDao.loadPracticesByProblemId(problemId);
    }

    @Override
    public void save(WarmupPractice warmupPractice) {
        Assert.notNull(warmupPractice, "待保存的练习不能为空");
        WarmupPractice origin = warmupPracticeDao.load(WarmupPractice.class, warmupPractice.getId());
        if(origin != null) {

            //解析或者题干有修改时,更新题目
            if(!warmupPractice.getAnalysis().equals(origin.getAnalysis()) ||
                    !warmupPractice.getQuestion().equals(origin.getQuestion())) {
                warmupPracticeDao.updateWarmupPractice(warmupPractice);
            }

            //更新额外信息
            warmupPracticeDao.updateExtraWarmupPractice(warmupPractice);

            //选项或者正确性有修改时,更新选项
            List<WarmupChoice> originChoices = warmupChoiceDao.loadChoices(origin.getId());
            warmupPractice.getChoiceList().forEach(warmupChoice -> {
                originChoices.stream().forEach(originChoice -> {
                    if(originChoice.getId() == warmupChoice.getId()) {
                        if(!originChoice.getIsRight().equals(warmupChoice.getIsRight()) ||
                                !originChoice.getSubject().equals(warmupChoice.getSubject())) {
                            warmupChoiceDao.updateChoice(warmupChoice);
                        }
                    }
                });
            });

        }
    }

    @Override
    public WarmupPractice getNextPractice(Integer problemId, Integer prePracticeId) {
        WarmupPractice warmupPractice = warmupPracticeDao.loadNextPractice(problemId, prePracticeId);
        if(warmupPractice != null) {
            List<WarmupChoice> choices = warmupChoiceDao.loadChoices(warmupPractice.getId());
            warmupPractice.setChoiceList(choices);
        }
        return warmupPractice;
    }

    @Override
    public Integer deleteAsstWarmupDiscuss(Integer discussId) {
        WarmupPracticeDiscuss warmupPracticeDiscuss = warmupPracticeDiscussDao.load(WarmupPracticeDiscuss.class, discussId);
        Integer profileId = warmupPracticeDiscuss.getProfileId();
        if(profileId != null) {
            List<UserRole> userRoleList = userRoleDao.getRoles(profileId);
            Long cnt = userRoleList.stream().filter(userRole -> Role.isAsst(userRole.getRoleId())).count();
            if(cnt > 0) {
                warmupPracticeDiscussDao.deleteDiscussById(discussId);
                String url = "/rise/static/message/warmup/reply?commentId={0}&warmupPracticeId={1}";
                url = MessageFormat.format(url, Objects.toString(warmupPracticeDiscuss.getId()), Objects.toString(warmupPracticeDiscuss.getWarmupPracticeId()));
                messageService.sendMessage("糟糕，由于不符合助教行为规范，你的留言已被管理员删除，有疑问请在助教群提出。",
                        Objects.toString(warmupPracticeDiscuss.getProfileId()), SYSTEM_MESSAGE, url);
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

}
