package com.iquanwai.confucius.biz.domain.util;

import com.iquanwai.confucius.biz.dao.quanwai.EmployeeDao;
import com.iquanwai.confucius.biz.po.quanwai.QuanwaiEmployee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService{

    @Autowired
    private EmployeeDao employeeDao;

    @Override
    public List<QuanwaiEmployee> getEmployees() {
        return employeeDao.getEmployees();
    }
}
