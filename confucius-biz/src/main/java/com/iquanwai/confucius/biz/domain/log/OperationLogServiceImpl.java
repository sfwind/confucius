package com.iquanwai.confucius.biz.domain.log;

import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.common.log.ActionLogDao;
import com.iquanwai.confucius.biz.dao.common.log.OperationLogDao;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.confucius.biz.po.ActionLog;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.ThreadPool;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.support.Assert;

import java.util.Map;
import java.util.function.Supplier;

@Service
public class OperationLogServiceImpl implements OperationLogService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OperationLogDao operationLogDao;
    @Autowired
    private ActionLogDao actionLogDao;
    @Autowired
    private SensorsAnalytics sa;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private UserRoleDao userRoleDao;

    @Override
    public void log(OperationLog operationLog) {
        if (ConfigUtils.logSwitch()) {
            if (operationLog.getMemo() != null && operationLog.getMemo().length() > 1024) {
                operationLog.setMemo(operationLog.getMemo().substring(0, 1024));
            }
            operationLogDao.insert(operationLog);
        }
    }

    @Override
    public void log(ActionLog actionLog) {
        if (ConfigUtils.logSwitch()) {
            if (actionLog.getMemo() != null && actionLog.getMemo().length() > 1024) {
                actionLog.setMemo(actionLog.getMemo().substring(0, 1024));
            }
            actionLogDao.insert(actionLog);
        }
    }

    @Override
    public void trace(Supplier<Integer> profileIdSupplier, String eventName, Supplier<Prop> supplier) {
        ThreadPool.execute(() -> {
            try {
                Integer profileId = profileIdSupplier.get();
                Prop prop = supplier.get();
                Map<String, Object> properties = prop.build();
                Assert.notNull(profileId, "用户id不能为null");
                Profile profile = profileDao.load(Profile.class, profileId);
                UserRole role = userRoleDao.loadAssist(profileId);

                Integer roleName = 0;
                RiseMember validRiseMember = riseMemberDao.loadValidRiseMember(profileId);

                RiseClassMember riseClassMember = riseClassMemberDao.loadActiveRiseClassMember(profileId);
                if (riseClassMember == null) {
                    riseClassMember = riseClassMemberDao.loadLatestRiseClassMember(profileId);
                }

                if (riseClassMember != null) {
                    properties.put("className", riseClassMember.getClassName());
                    properties.put("groupId", riseClassMember.getGroupId());
                }
                if (validRiseMember != null) {
                    roleName = validRiseMember.getMemberTypeId();
                }
                properties.put("roleName", roleName);
                properties.put("isAsst", role != null);
                properties.put("riseId", profile.getRiseId());

                sa.track(profileId.toString(), true, eventName, properties);
                // TODO 上线前删掉
                sa.flush();
            } catch (InvalidArgumentException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        });
    }

    @Override
    public void trace(Integer profileId, String eventName) {
        this.trace(() -> profileId, eventName, OperationLogService::props);
    }

    @Override
    public void trace(Supplier<Integer> supplier, String eventName) {
        this.trace(supplier, eventName, OperationLogService::props);
    }

    @Override
    public void trace(Integer profileId, String eventName, Supplier<Prop> supplier) {
        this.trace(() -> profileId, eventName, supplier);
    }
}
