package com.iquanwai.confucius.web.pc.asst.controller;

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
        //TODO:生成候选的Dto
          if(asstUpExecution.getRoleId()==11){
              return genCandidate(profileId,asstUpStandard,asstUpExecution);
          }
          if(asstUpExecution.getRoleId()==3){
              return genProbationary(profileId,asstUpStandard,asstUpExecution);
          }
          return null;
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

    /**
     * 生成候选的内容
     * @param profileId
     * @param asstUpStandard
     * @param asstUpExecution
     * @return
     */
    private UpGradeDto genCandidate(Integer profileId,AsstUpStandard asstUpStandard,AsstUpExecution asstUpExecution){

        UpGradeDto upGradeDto = new UpGradeDto();

        Integer interval = DateUtils.interval(asstUpExecution.getStartDate());
        Integer countDown = asstUpStandard.getCountDown();
        upGradeDto.setRemainDay(getRemain(interval,countDown));
        //统计已经完成的课程数
        Long result = planService.getUserPlans(profileId).stream().filter(improvementPlan -> improvementPlan.getCompleteTime()!=null).count();
        Integer finish = result.intValue();
        Integer total = asstUpStandard.getLearnedProblem();
        upGradeDto.setLearnedProblem(finish);
        upGradeDto.setRemainProblem(getRemain(finish,total));

        finish = asstUpExecution.getReviewNumber();
        total = asstUpStandard.getReviewNumber();
        upGradeDto.setReviewedNumber(finish);
        upGradeDto.setRemainReviewNumber(getRemain(finish,total));
        Integer valid = asstUpExecution.getValidReviewNumber();
        if(finish==0) {
            upGradeDto.setReviewRate(0);
        } else{
            upGradeDto.setReviewRate(valid * 100 / finish);
        }
        finish = asstUpExecution.getHighQualityAnswer();
        total = asstUpStandard.getHighQualityAnswer();
        upGradeDto.setHighAnswer(finish);
        upGradeDto.setRemainHighAnswer(getRemain(finish,total));

        return upGradeDto;
    }

    /**
     * 生成见习的内容
     * @param profileId
     * @param asstUpStandard
     * @param asstUpExecution
     * @return
     */
    private UpGradeDto genProbationary(Integer profileId,AsstUpStandard asstUpStandard,AsstUpExecution asstUpExecution){
        UpGradeDto upGradeDto = genCandidate(profileId,asstUpStandard,asstUpExecution);
        Integer finish = asstUpExecution.getHostNumber();
        Integer total = asstUpStandard.getHostNumber();
        upGradeDto.setHostNumber(finish);
        upGradeDto.setRemainHostNumber(getRemain(finish,total));
        upGradeDto.setHostScore(asstUpExecution.getHostScore());
        upGradeDto.setOnlineAnswer(asstUpExecution.getOnlineAnswer());

        return upGradeDto;
    }
}
