package com.iquanwai.confucius.web.pc.datahelper;

import com.iquanwai.confucius.biz.po.asst.AsstUpExecution;
import com.iquanwai.confucius.biz.po.asst.AsstUpStandard;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.web.pc.asst.dto.UpGradeDto;

public class AsstHelper {

    public static Boolean checkIsReached(AsstUpStandard asstUpStandard, AsstUpExecution asstUpExecution){
        if (asstUpStandard.getRequestReviewNumber() > asstUpExecution.getRequestReviewNumber()) {
            return false;
        }
        Integer rate;
        if (asstUpExecution.getReviewNumber() == 0) {
            rate = 0;
        } else {
            rate =  (asstUpExecution.getValidReviewNumber() * 100)/asstUpExecution.getReviewNumber();
        }
        if(asstUpStandard.getValidReviewNumber()>asstUpExecution.getValidReviewNumber()){
            return false;
        }
        if(asstUpStandard.getValidReviewRate()>rate){
            return false;
        }
        if(asstUpStandard.getHighQualityAnswer()>asstUpExecution.getHighQualityAnswer()){
            return false;
        }
        if(asstUpStandard.getHostNumber()>asstUpExecution.getHostNumber()){
            return false;
        }
        if(asstUpStandard.getHostScore().compareTo(asstUpExecution.getHostScore())>0){
            return false;
        }
        if(asstUpStandard.getMainPointNumber()>asstUpExecution.getMainPointNumber()){
            return false;
        }
        if(asstUpStandard.getMainPointScore().compareTo(asstUpExecution.getMainPointScore())>0){
            return false;
        }
        if(asstUpStandard.getOnlineAnswer().equals("Y") && asstUpExecution.getOnlineAnswer().equals("N")){
            return false;
        }
        if(asstUpStandard.getSwing().equals("Y") && asstUpExecution.getSwing().equals("N")){
            return false;
        }
        if(asstUpStandard.getOnlineOrSwingNumber()>asstUpExecution.getOnlineOrSwingNumber()){
            return false;
        }
        if(asstUpStandard.getOnlineScore().compareTo(asstUpExecution.getOnlineScore())>0){
            return false;
        }
        if(asstUpStandard.getCampNumber()>asstUpExecution.getCampNumber()){
            return false;
        }
        if(asstUpStandard.getAsstNumber()>asstUpExecution.getAsstNumber()){
            return false;
        }
        if(asstUpStandard.getCampScore().compareTo(asstUpExecution.getCampScore())>0){
            return false;
        }
        if(asstUpStandard.getMonthlyWork().equals("Y") && asstUpExecution.getMonthlyWork().equals("N")){
            return false;
        }
        if(asstUpStandard.getFosterNew()>asstUpExecution.getFosterNew()){
            return false;
        }
        if(asstUpStandard.getCompanyTrainNumber()>asstUpExecution.getCompanyTrainNumber()){
            return false;
        }
        if(asstUpStandard.getCompanyTrainScore().compareTo(asstUpExecution.getCompanyTrainScore())>0){
            return false;
        }
        return true;
    }

    public static UpGradeDto genUpGradeInfo(AsstUpStandard asstUpStandard,AsstUpExecution asstUpExecution){
        UpGradeDto upGradeDto = new UpGradeDto();

        Integer interval = DateUtils.interval(asstUpExecution.getStartDate());
        Integer countDown = asstUpStandard.getCountDown();
        upGradeDto.setStartDate(asstUpExecution.getStartDate());
        upGradeDto.setCountDown(countDown);
        upGradeDto.setRemainDay(getRemain(interval,countDown));

        Integer finish;
        Integer total;

        finish = asstUpExecution.getReviewNumber();
        upGradeDto.setReviewedNumber(finish);
        Integer valid = asstUpExecution.getValidReviewNumber();
        total = asstUpStandard.getRequestReviewNumber();
        finish = asstUpExecution.getReviewNumber();
        upGradeDto.setNeedRequestReviewNumber(total);
        upGradeDto.setRequestReviewNumber(finish);
        upGradeDto.setRemainRequestReviewNumber(getRemain(finish,total));
        upGradeDto.setNeedValidReviewNumber(asstUpStandard.getValidReviewNumber());
        upGradeDto.setValidReviewNumber(asstUpExecution.getValidReviewNumber());
        upGradeDto.setNeedReviewRate(asstUpStandard.getValidReviewRate());
        if(finish==0) {
            upGradeDto.setReviewRate(0);
        } else{
            upGradeDto.setReviewRate(valid * 100 / finish);
        }
        finish = asstUpExecution.getHighQualityAnswer();
        total = asstUpStandard.getHighQualityAnswer();
        upGradeDto.setNeedHighAnswer(total);
        upGradeDto.setHighAnswer(finish);
        upGradeDto.setRemainHighAnswer(getRemain(finish,total));

        total = asstUpStandard.getHostNumber();
        finish = asstUpExecution.getHostNumber();

        upGradeDto.setNeedHostNumber(total);
        upGradeDto.setHostNumber(finish);
        upGradeDto.setRemainHostNumber(getRemain(finish,total));

        upGradeDto.setNeedHostScore(asstUpStandard.getHostScore());
        upGradeDto.setHostScore(asstUpExecution.getHostScore());

        total = asstUpStandard.getMainPointNumber();
        finish = asstUpExecution.getMainPointNumber();
        upGradeDto.setNeedMainPointNumber(total);
        upGradeDto.setMainPointNumber(finish);
        upGradeDto.setRemainPointNumber(getRemain(finish,total));

        upGradeDto.setNeedPointScore(asstUpStandard.getMainPointScore());
        upGradeDto.setMainPointScore(asstUpExecution.getMainPointScore());
        upGradeDto.setNeedOnlineAnswer(asstUpStandard.getOnlineAnswer());
        upGradeDto.setOnlineAnswer(asstUpExecution.getOnlineAnswer());
        upGradeDto.setNeedSwing(asstUpStandard.getSwing());
        upGradeDto.setSwing(asstUpExecution.getSwing());

        total = asstUpStandard.getOnlineOrSwingNumber();
        finish = asstUpExecution.getOnlineOrSwingNumber();
        upGradeDto.setNeedOnlineNumber(total);
        upGradeDto.setOnlineOrSwingNumber(finish);
        upGradeDto.setRemainOnlineOrSwingNumber(getRemain(finish,total));
        upGradeDto.setNeedOnlineScore(asstUpStandard.getOnlineScore());
        upGradeDto.setOnlineScore(asstUpExecution.getOnlineScore());
        total = asstUpStandard.getCampNumber();
        finish = asstUpExecution.getCampNumber();
        upGradeDto.setNeedCampNumber(total);
        upGradeDto.setCampNumber(finish);
        upGradeDto.setRemainCampNumber(getRemain(finish,total));
        total = asstUpStandard.getAsstNumber();
        finish = asstUpExecution.getAsstNumber();
        upGradeDto.setNeedAsstNumber(total);
        upGradeDto.setAsstNumber(finish);
        upGradeDto.setRemainAsstNumber(getRemain(finish,total));
        upGradeDto.setNeedCampScore(asstUpStandard.getCampScore());
        upGradeDto.setCampScore(asstUpExecution.getCampScore());
        upGradeDto.setNeedMonthlyWork(asstUpStandard.getMonthlyWork());
        upGradeDto.setMonthlyWork(asstUpExecution.getMonthlyWork());

        total = asstUpStandard.getFosterNew();
        finish = asstUpExecution.getFosterNew();
        upGradeDto.setNeedFosterNew(total);
        upGradeDto.setFosterNew(finish);
        upGradeDto.setRemainFosterNew(getRemain(finish,total));

        total = asstUpStandard.getCompanyTrainNumber();
        finish = asstUpExecution.getCompanyTrainNumber();
        upGradeDto.setNeedCompanyNumber(total);
        upGradeDto.setCompanyNumber(finish);
        upGradeDto.setRemainCompanyNumber(getRemain(finish,total));
        upGradeDto.setNeedCompanyScore(asstUpStandard.getCompanyTrainScore());
        upGradeDto.setCompanyTrainScore(asstUpExecution.getCompanyTrainScore());
        upGradeDto.setNeedVerified(asstUpStandard.getNeedVerified());
        upGradeDto.setUpGrade(asstUpExecution.getUpGrade());
        return upGradeDto;

    }



    /**
     * 计算剩余需要的完成数（如果已经完成的大于所有的，则返回0）
     * @param finished
     * @param totalNum
     * @return
     */
    public static Integer getRemain(Integer finished,Integer totalNum){
        if(totalNum<finished){
            return 0;
        }else {
            return totalNum-finished;
        }
    }

}
