package com.iquanwai.confucius.biz.domain.util;

import com.iquanwai.confucius.biz.po.quanwai.QuanwaiEmployee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService{

    @Autowired
    private EmployeeService employeeService;

    @Override
    public List<QuanwaiEmployee> getEmployees() {
        return employeeService.getEmployees();
    }
}
