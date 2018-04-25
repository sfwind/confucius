package com.iquanwai.confucius.web.pc.backend.controller;

import com.iquanwai.confucius.biz.domain.backend.problem.PreviewService;
import com.iquanwai.confucius.biz.domain.backend.problem.ScheduleService;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemPreview;
import com.iquanwai.confucius.biz.po.fragmentation.ProblemSchedule;
import com.iquanwai.confucius.web.pc.backend.dto.PreviewDto;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pc/operation/preview")
public class PreviewController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private PreviewService previewService;

    @RequestMapping(value = "/load/description/{problemId}",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> loadDescription(@PathVariable Integer problemId,@RequestParam("chapter")Integer chapter,@RequestParam("section")Integer section){
        ProblemSchedule problemSchedule = scheduleService.loadProblemSchedule(problemId, chapter, section);
        if(problemSchedule==null){
            return WebUtils.error("没有该章节");
        }else{
            ProblemPreview problemPreview = previewService.loadByProblemScheduleId(problemSchedule.getId());
            if(problemPreview!=null){
                return WebUtils.result(problemPreview.getDescription());
            }else{
                return WebUtils.success();
            }
        }
    }


    @RequestMapping(value = "/update/{problemId}",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> updatePreviews(@PathVariable Integer problemId, @RequestParam ("param")PreviewDto previewDto){
        Integer chapter = previewDto.getChapter();
        Integer section = previewDto.getSection();

        ProblemSchedule problemSchedule = scheduleService.loadProblemSchedule(problemId,chapter,section);
        if(problemSchedule==null){
            return WebUtils.error("不存在该章节，请先在知识点导入处导入该章节内容");
        }

        ProblemPreview problemPreview = new ProblemPreview();
        BeanUtils.copyProperties(previewDto,problemPreview);
        problemPreview.setProblemScheduleId(problemSchedule.getId());

        Integer result = previewService.updatePreview(problemPreview);

        if(result>0){
            return WebUtils.success();
        }
        return WebUtils.error("添加失败");
    }



}

