package com.iquanwai.confucius.web.pc.asst.controller;

import com.google.zxing.oned.UPCAReader;
import com.iquanwai.confucius.biz.domain.asst.AsstUpService;
import com.iquanwai.confucius.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.asst.AsstUpExecution;
import com.iquanwai.confucius.biz.po.asst.AsstUpStandard;
import com.iquanwai.confucius.biz.util.DateUtils;
import com.iquanwai.confucius.web.pc.asst.dto.UpGradeDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/pc/asst")
public class AsstUpGradeController {

    @Autowired
    private AsstUpService asstUpService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PlanService planService;

    /**
     * @param loginUser
     * @return
     */
    @RequestMapping("/load/up/info")
    public ResponseEntity<Map<String, Object>> loadUpGradeInfo(PCLoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("助教后台管理")
                .function("助教信息").action("加载升级信息");
        operationLogService.log(operationLog);

        AsstUpExecution asstUpExecution = asstUpService.loadUpGradeExecution(loginUser.getProfileId());
        AsstUpStandard asstUpStandard = asstUpService.loadStandard(loginUser.getProfileId());

        UpGradeDto upGradeDto = initUpGradeDto(loginUser.getProfileId(),asstUpStandard,asstUpExecution);

        return WebUtils.result(upGradeDto);
    }


    private UpGradeDto initUpGradeDto(Integer profileId,AsstUpStandard asstUpStandard,AsstUpExecution asstUpExecution){
        UpGradeDto upGradeDto = new UpGradeDto();

        Integer interval = DateUtils.interval(asstUpExecution.getStartDate());
        Integer countDown = asstUpStandard.getCountDown();
        upGradeDto.setStartDate(asstUpExecution.getStartDate());
        upGradeDto.setCountDown(countDown);
        upGradeDto.setRemainDay(getRemain(interval,countDown));
        //统计已经完成的课程数
        Long result = planService.getUserPlans(profileId).stream().filter(improvementPlan -> improvementPlan.getCompleteTime()!=null).count();
        Integer finish = result.intValue();
        Integer total = asstUpStandard.getLearnedProblem();
        upGradeDto.setNeedLearnedProblem(total);
        upGradeDto.setLearnedProblem(finish);
        upGradeDto.setRemainProblem(getRemain(finish,total));
        finish = asstUpExecution.getReviewNumber();
        total = asstUpStandard.getReviewNumber();
        upGradeDto.setNeedReviewedNumber(total);
        upGradeDto.setReviewedNumber(finish);
        upGradeDto.setRemainReviewNumber(getRemain(finish,total));
        Integer valid = asstUpExecution.getValidReviewNumber();
        total = asstUpStandard.getRequestReviewNumber();
        finish = asstUpExecution.getReviewNumber();
        upGradeDto.setNeedRequestReviewNumber(total);
        upGradeDto.setRequestReviewNumber(finish);
        upGradeDto.setRemainRequestReviewNumber(getRemain(finish,total));
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
        upGradeDto.setCompanyScore(asstUpExecution.getCompanyTrainScore());

        return upGradeDto;

    }

    /**
     * 计算剩余需要的完成数（如果已经完成的大于所有的，则返回0）
     * @param finished
     * @param totalNum
     * @return
     */
    private Integer getRemain(Integer finished,Integer totalNum){
        if(totalNum<finished){
            return 0;
        }else {
            return totalNum-finished;
        }
    }
}
