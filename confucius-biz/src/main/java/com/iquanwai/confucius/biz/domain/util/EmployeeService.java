package com.iquanwai.confucius.biz.domain.util;

import com.iquanwai.confucius.biz.po.quanwai.QuanwaiEmployee;

import java.util.List;

public interface EmployeeService {

    /**
     * 获得圈外员工
     * @return
     */
     List<QuanwaiEmployee> getEmployees();

}
