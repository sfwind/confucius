package com.iquanwai.confucius.web.pc.backend.controller;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.domain.log.OperationLogService;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.util.zk.ConfigNode;
import com.iquanwai.confucius.biz.util.zk.ZKConfigUtils;
import com.iquanwai.confucius.web.pc.backend.dto.ConfigDto;
import com.iquanwai.confucius.web.resolver.PCLoginUser;
import com.iquanwai.confucius.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/3/29.
 * 管理员相关的接口
 */
@RestController
@RequestMapping("/pc/admin")
public class AdminController {
    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private ZKConfigUtils zkConfigUtils;

    @RequestMapping("/config/{projectId}")
    public ResponseEntity<Map<String, Object>> loadConfig(PCLoginUser pcLoginUser,
                                                          @PathVariable String projectId) {

        Assert.notNull(pcLoginUser, "用户不能为空");
        List<ConfigNode> configNodeList = zkConfigUtils.getAllValue(projectId);
        List<ConfigDto> configDtoList = Lists.newArrayList();
        configNodeList.stream().forEach(configNode -> {
            ConfigDto configDto = new ConfigDto();
            configDto.setProjectId(projectId);
            configDto.setKey(configNode.getKey());
            configDto.setValue(configNode.getValue());
            configDto.setDesc(configNode.getDesc());
            configDtoList.add(configDto);
        });
        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("管理员")
                .function("后台配置")
                .action("查询配置项")
                .memo(projectId);
        operationLogService.log(operationLog);

        return WebUtils.result(configDtoList);
    }

    @RequestMapping(value = "/config/update", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateConfig(PCLoginUser pcLoginUser,
                                                            @RequestBody ConfigDto configDto) {

        Assert.notNull(pcLoginUser, "用户不能为空");
        zkConfigUtils.updateValue(configDto.getProjectId(), configDto.getKey(),
                configDto.getValue(), configDto.getDesc());

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("管理员")
                .function("后台配置")
                .action("更新配置项")
                .memo(configDto.getKey() + ':' + configDto.getValue());
        operationLogService.log(operationLog);

        return WebUtils.success();
    }

    @RequestMapping(value = "/config/add", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addConfig(PCLoginUser pcLoginUser,
                                                         @RequestBody ConfigDto configDto) {

        Assert.notNull(pcLoginUser, "用户不能为空");
        zkConfigUtils.createValue(configDto.getProjectId(), configDto.getKey(),
                configDto.getValue(), configDto.getDesc());

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("管理员")
                .function("后台配置")
                .action("增加配置项")
                .memo(configDto.getKey() + ':' + configDto.getValue());
        operationLogService.log(operationLog);

        return WebUtils.success();
    }

    @RequestMapping(value = "/config/delete", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> deleteConfig(PCLoginUser pcLoginUser,
                                                            @RequestBody ConfigDto configDto) {

        Assert.notNull(pcLoginUser, "用户不能为空");
        zkConfigUtils.deleteValue(configDto.getProjectId(), configDto.getKey());

        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("管理员")
                .function("后台配置")
                .action("删除配置项")
                .memo(configDto.getKey());
        operationLogService.log(operationLog);

        return WebUtils.success();
    }

    @PreDestroy
    public void destroy() {
        zkConfigUtils.destroy();
    }
}
