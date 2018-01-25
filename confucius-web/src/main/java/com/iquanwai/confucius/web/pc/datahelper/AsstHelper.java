package com.iquanwai.confucius.web.pc.datahelper;

import com.iquanwai.confucius.biz.po.asst.AsstUpExecution;
import com.iquanwai.confucius.biz.po.asst.AsstUpStandard;

public class AsstHelper {

    public static Boolean checkIsReached(AsstUpStandard asstUpStandard, AsstUpExecution asstUpExecution){
        if (asstUpStandard.getReviewNumber() > asstUpExecution.getReviewNumber()) {
            return false;
        }
        if (asstUpStandard.getRequestReviewNumber() > asstUpExecution.getRequestReviewNumber()) {
            return false;
        }
        Integer rate;
        if (asstUpExecution.getReviewNumber() == 0) {
            rate = 0;
        } else {
            rate =  (asstUpExecution.getValidReviewNumber() * 100)/asstUpExecution.getReviewNumber();
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

}
