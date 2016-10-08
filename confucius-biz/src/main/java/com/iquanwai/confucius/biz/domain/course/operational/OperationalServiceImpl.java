package com.iquanwai.confucius.biz.domain.course.operational;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.course.ClassMemberDao;
import com.iquanwai.confucius.biz.po.ClassMember;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by justin on 16/10/8.
 */
@Service
public class OperationalServiceImpl implements OperationalService {
    @Autowired
    private ClassMemberDao classMemberDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public boolean angelAssign(Integer classId) {
        List<ClassMember> classMemberList = classMemberDao.getClassMember(classId);
        int classSize = classMemberList.size();
        if(classSize==0){
            logger.error("{} has no members", classId);
            return false;
        }
        if(classSize%2==1){
            logger.error("{} has {} members, is not an even number", classId, classSize);
            return false;
        }
        ClassMember[] classMemberArray = new ClassMember[classSize];
        for(int i=0;i<classSize;i++){
            classMemberArray[i]=classMemberList.get(i);
        }
        // 所有成员分成2组
        Pair<ClassMember[], ClassMember[]> classMemberGroup = groupMembers(classMemberArray);

        // 2组从对面组中挑选天使，返回结果中right是left的天使
        List<Pair<ClassMember, ClassMember>> firstPairs = assignAngel(classMemberGroup.getLeft(), classMemberGroup.getRight());
        List<Pair<ClassMember, ClassMember>> secondPairs = assignAngel(classMemberGroup.getRight(), classMemberGroup.getLeft());
        firstPairs.addAll(secondPairs);
        sendAngelAssignMessage(firstPairs);

        logger.info("assign angel for class {} complete", classId);
        return true;
    }

    private void sendAngelAssignMessage(List<Pair<ClassMember, ClassMember>> all) {

    }

    private static List<Pair<ClassMember, ClassMember>> assignAngel(ClassMember[] left, ClassMember[] right) {
        List<Pair<ClassMember, ClassMember>> pairs = Lists.newArrayList();
        int size = right.length;
        ClassMember[] temp = new ClassMember[size];
        System.arraycopy(right, 0, temp, 0, size);

        for(int i=0;i<left.length;i++){
            ClassMember member = left[i];
            // 取出一个随机数
            int r = (int) (Math.random() * size);
            ClassMember angel = temp[r];
            // 排除已经取过的值
            temp[r] = temp[size - 1];
            size--;

            pairs.add(new ImmutablePair<ClassMember, ClassMember>(member, angel));
        }

        return pairs;
    }

    private static Pair<ClassMember[], ClassMember[]> groupMembers(ClassMember[] classMemberArray) {
        int size = classMemberArray.length;
        ClassMember[] left = new ClassMember[size/2];

        int tempSize = size;

        for (int i = 0; i < left.length; i++) {
            // 取出一个随机数
            int r = (int) (Math.random() * tempSize);
            left[i] = classMemberArray[r];

            // 排除已经取过的值
            classMemberArray[r] = classMemberArray[tempSize - 1];
            tempSize--;
        }

        ClassMember[] right = new ClassMember[size/2];
        System.arraycopy(classMemberArray, 0, right, 0, size/2);

        return new ImmutablePair<ClassMember[], ClassMember[]>(left, right);
    }

//    public static void main(String[] args) {
//        ClassMember[] classMembers = new ClassMember[50];
//        for(int i=0;i<50;i++){
//            ClassMember classMember = new ClassMember();
//            classMember.setMemberId(""+i);
//            classMembers[i]=classMember;
//        }
//
//        Pair<ClassMember[], ClassMember[]> classMemberGroup = groupMembers(classMembers);
//
//        List<Pair<ClassMember, ClassMember>> firstPairs = assignAngel(classMemberGroup.getLeft(), classMemberGroup.getRight());
//        List<Pair<ClassMember, ClassMember>> secondPairs = assignAngel(classMemberGroup.getRight(), classMemberGroup.getLeft());
//        firstPairs.addAll(secondPairs);
//
//        for(Pair<ClassMember, ClassMember> pair:firstPairs){
//            System.out.println(pair.getRight().getMemberId() + " is " + pair.getLeft().getMemberId() + " angel");
//        }
//    }
}
