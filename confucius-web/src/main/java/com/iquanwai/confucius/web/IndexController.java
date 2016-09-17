package com.iquanwai.confucius.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by justin on 16/9/9.
 */
@Controller
public class IndexController {
    @RequestMapping(value = "/static/**",method = RequestMethod.GET)
    public ModelAndView getIndex() {
        return new ModelAndView("course");
    }
}
