package com.iquanwai.confucius.biz.domain.course.operational;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.course.ClassDao;
import com.iquanwai.confucius.biz.dao.course.ClassMemberDao;
import com.iquanwai.confucius.biz.dao.course.CourseDao;
import com.iquanwai.confucius.biz.dao.operational.AngelDao;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.confucius.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.confucius.biz.po.Angel;
import com.iquanwai.confucius.biz.po.ClassMember;
import com.iquanwai.confucius.biz.po.Course;
import com.iquanwai.confucius.biz.po.QuanwaiClass;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.DateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/10/8.
 */
@Service
public class OperationalServiceImpl implements OperationalService {
    @Autowired
    private ClassMemberDao classMemberDao;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private ClassDao classDao;
    @Autowired
    private CourseDao courseDao;
    @Autowired
    private AngelDao angelDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public boolean angelAssign(Integer classId) {
        List<ClassMember> classMemberList = classMemberDao.getClassMember(classId);
        int classSize = classMemberList.size();
        if(classSize==0){
            logger.error("{} has no members", classId);
            return false;
        }
        ClassMember[] classMemberArray = new ClassMember[classSize];
        for(int i=0;i<classSize;i++){
            classMemberArray[i]=classMemberList.get(i);
        }
        // 所有成员分成2组
        List<Pair<ClassMember, ClassMember>> pairs = assignAngel(classMemberArray);

        sendAngelAssignMessage(pairs, classId);

        logger.info("assign angel for class {} complete", classId);
        return true;
    }

    public void angelAssign() {
        //获取明天开班的所有班级
        List<QuanwaiClass> quanwaiClasses = classDao.loadClassByOpenDate(DateUtils.afterDays(new Date(), 1));

        for(QuanwaiClass quanwaiClass:quanwaiClasses){
            angelAssign(quanwaiClass.getId());
        }
    }

    private void sendAngelAssignMessage(List<Pair<ClassMember, ClassMember>> all, int classId) {
        QuanwaiClass quanwaiClass = classDao.load(QuanwaiClass.class, classId);
        if(quanwaiClass==null){
            logger.error("classId {} is not found", classId);
            return;
        }
        Course course = courseDao.load(Course.class, quanwaiClass.getCourseId());
        if(course==null){
            logger.error("the courseId of class {} is invalid", classId);
            return;
        }
        for(Pair<ClassMember, ClassMember> pair:all) {
            Angel angel = new Angel();
            angel.setMemberId(pair.getLeft().getMemberId());
            angel.setAngelId(pair.getRight().getMemberId());
            angel.setClassId(pair.getLeft().getClassId());
            angelDao.insert(angel);
            sendAngelMessage(pair, quanwaiClass, course.getName());
        }
    }

    private void sendAngelMessage(Pair<ClassMember, ClassMember> pair, QuanwaiClass quanwaiClass, String courseName) {
        Assert.notNull(quanwaiClass, "班级不能为空");
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(pair.getLeft().getOpenId());

        templateMessage.setTemplate_id(ConfigUtils.angelMsgKey());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);

        String date = DateUtils.parseDateToString(DateUtils.beforeDays(quanwaiClass.getOpenTime(), 1));
        if(quanwaiClass!=null) {
            String first = "今晚微信群里，圈圈带大家开启本期的训练营咯，记得准时参加！";
            String remark = "记住这个号码：{number}；你是这个号码学员的天使哦！什么是天使？晚上圈圈告诉你。\n\n还没加群？点击查看群二维码。";
            data.put("first", new TemplateMessage.Keyword(first));
            data.put("keyword1", new TemplateMessage.Keyword(courseName+"训练营开营仪式"));
            data.put("keyword2", new TemplateMessage.Keyword(date+" 晚上8：30"));
            data.put("remark", new TemplateMessage.Keyword(remark.replace("{number}", angelNumber(pair.getRight().getMemberId()))));

            templateMessage.setUrl(quanwaiClass.getWeixinGroup());
            templateMessageService.sendMessage(templateMessage);
        }
    }

    private static String angelNumber(String memberId) {
        if(StringUtils.isEmpty(memberId)){
            return "";
        }
        if(memberId.length()<=3){
            return Integer.valueOf(memberId).toString();
        }
        return Integer.valueOf(memberId.substring(memberId.length()-3)).toString();
    }

    private static List<Pair<ClassMember, ClassMember>> assignAngel(ClassMember[] classMembers) {
        //少于2个人，没法分配天使
        if(classMembers.length<=1){
            return Lists.newArrayList();
        }
        List<Pair<ClassMember, ClassMember>> pairs = Lists.newArrayList();

        ClassMember[] angels = new ClassMember[classMembers.length];
        boolean dup = true;
        while(dup) {
            // init
            int size = classMembers.length;
            ClassMember[] temp = new ClassMember[size];
            angels = new ClassMember[size];
            System.arraycopy(classMembers, 0, temp, 0, size);

            for (int i = 0; i < temp.length; i++) {
                // 取出一个随机数
                int r = (int) (Math.random() * size);
                ClassMember angel = temp[r];
                // 排除已经取过的值
                temp[r] = temp[size - 1];
                size--;
                angels[i] = angel;
            }
            //校验是否某一个人是自己的天使，如果校验不通过，重新分配
            dup = checkDuplidate(classMembers, angels);
        }

        for(int j=0;j<classMembers.length;j++){
            pairs.add(new ImmutablePair<ClassMember, ClassMember>(classMembers[j], angels[j]));
        }

        return pairs;
    }

    private static boolean checkDuplidate(ClassMember[] classMembers, ClassMember[] angels) {
        for(int i=0;i<classMembers.length;i++){
            if(classMembers[i].getMemberId().equals(angels[i].getMemberId())){
                return true;
            }
        }
        return false;
    }

//    public static void main(String[] args) {
//        ClassMember[] classMembers = new ClassMember[100];
//        for(int i=0;i<100;i++){
//            ClassMember classMember = new ClassMember();
//            classMember.setMemberId(""+i);
//            classMembers[i]=classMember;
//        }
//
//        List<Pair<ClassMember, ClassMember>> classMemberGroup = assignAngel(classMembers);
//
//        for(Pair<ClassMember, ClassMember> pair:classMemberGroup){
//            System.out.println(pair.getRight().getMemberId() + " is " + pair.getLeft().getMemberId() + " angel");
//        }
//    }
}
