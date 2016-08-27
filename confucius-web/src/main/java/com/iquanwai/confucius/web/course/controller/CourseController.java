package com.iquanwai.confucius.web.course.controller;

import com.iquanwai.confucius.biz.dao.po.Chapter;
import com.iquanwai.confucius.biz.dao.po.Course;
import com.iquanwai.confucius.resolver.LoginUser;
import com.iquanwai.confucius.util.WebUtils;
import com.iquanwai.confucius.web.course.dto.CoursePageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/8/25.
 */
@Controller
@RequestMapping("/course")
public class CourseController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> loadCourse(LoginUser loginUser){
        CoursePageDto coursePageDto = new CoursePageDto();
        coursePageDto.setOpenid("oK881wQekezGpw6rq790y_vAY_YY");
        coursePageDto.setUsername("风之伤");
        coursePageDto.setUserProgress(2);
        coursePageDto.setWeek(1);
        Course course = new Course();
        coursePageDto.setCourse(course);
        course.setWeek(1);
        course.setName("结构化思维");
        course.setId(1);
        course.setPic("http://someurl");
        List<Chapter> list = new ArrayList<Chapter>();
        course.setChapterList(list);
        Chapter day1 = new Chapter();
        day1.setId(1);
        day1.setIcon("http://someurl");
        day1.setName("结构化思维基础（1）");
        day1.setUnlock(true);
        day1.setType(1);
        day1.setSequence(1);
        list.add(day1);

        Chapter day2 = new Chapter();
        day2.setId(2);
        day2.setIcon("http://someurl");
        day2.setName("结构化思维基础（2）");
        day2.setUnlock(true);
        day2.setType(1);
        day2.setSequence(2);
        list.add(day2);

        Chapter day3 = new Chapter();
        day3.setId(3);
        day3.setIcon("http://someurl");
        day3.setName("结构化思维基础（3）");
        day3.setUnlock(false);
        day3.setType(1);
        day3.setSequence(3);
        list.add(day3);

        Chapter day4 = new Chapter();
        day4.setId(4);
        day4.setIcon("http://someurl");
        day4.setName("大作业");
        day4.setUnlock(false);
        day4.setType(2);
        day4.setSequence(4);
        list.add(day4);

        Chapter day6 = new Chapter();
        day6.setId(5);
        day6.setIcon("http://someurl");
        day6.setName("8：00pm群里点评咯");
        day6.setUnlock(false);
        day6.setType(3);
        day6.setSequence(5);
        list.add(day6);

        Chapter day7 = new Chapter();
        day7.setId(6);
        day7.setIcon("http://someurl");
        day7.setName("休息日");
        day7.setUnlock(false);
        day7.setType(4);
        day7.setSequence(6);
        list.add(day6);
        return WebUtils.result(coursePageDto);
    }

    @RequestMapping("/week/{courseId}/{week}")
    public ResponseEntity<Map<String, Object>> loadWeek(LoginUser loginUser){
        CoursePageDto coursePageDto = new CoursePageDto();
        coursePageDto.setOpenid("sadjljda");
        coursePageDto.setUsername("风之伤");
        coursePageDto.setUserProgress(2);
        coursePageDto.setWeek(1);
        Course course = new Course();
        coursePageDto.setCourse(course);
        course.setWeek(1);
        course.setName("结构化思维");
        course.setId(1);
        course.setPic("http://someurl");
        List<Chapter> list = new ArrayList<Chapter>();
        course.setChapterList(list);
        Chapter day1 = new Chapter();
        day1.setId(1);
        day1.setIcon("http://someurl");
        day1.setName("结构化思维基础（1）");
        day1.setUnlock(true);
        day1.setType(1);
        day1.setSequence(1);
        list.add(day1);

        Chapter day2 = new Chapter();
        day2.setId(2);
        day2.setIcon("http://someurl");
        day2.setName("结构化思维基础（2）");
        day2.setUnlock(true);
        day2.setType(1);
        day2.setSequence(2);
        list.add(day2);

        Chapter day3 = new Chapter();
        day3.setId(3);
        day3.setIcon("http://someurl");
        day3.setName("结构化思维基础（3）");
        day3.setUnlock(false);
        day3.setType(1);
        day3.setSequence(3);
        list.add(day3);

        Chapter day4 = new Chapter();
        day4.setId(4);
        day4.setIcon("http://someurl");
        day4.setName("大作业");
        day4.setUnlock(false);
        day4.setType(2);
        day4.setSequence(4);
        list.add(day4);

        Chapter day6 = new Chapter();
        day6.setId(5);
        day6.setIcon("http://someurl");
        day6.setName("8：00pm群里点评咯");
        day6.setUnlock(false);
        day6.setType(3);
        day6.setSequence(5);
        list.add(day6);

        Chapter day7 = new Chapter();
        day7.setId(6);
        day7.setIcon("http://someurl");
        day7.setName("休息日");
        day7.setUnlock(false);
        day7.setType(4);
        day7.setSequence(6);
        list.add(day6);
        return WebUtils.result(coursePageDto);
    }
}
