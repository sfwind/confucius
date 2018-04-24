package com.iquanwai.confucius.biz.domain.log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.customer.RiseMemberDao;
import com.iquanwai.confucius.biz.dao.common.log.ActionLogDao;
import com.iquanwai.confucius.biz.dao.common.log.OperationLogDao;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.dao.fragmentation.FragmentClassMemberDao;
import com.iquanwai.confucius.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.confucius.biz.domain.course.signup.RiseMemberManager;
import com.iquanwai.confucius.biz.domain.course.signup.RiseMemberTypeRepo;
import com.iquanwai.confucius.biz.domain.fragmentation.ClassMember;
import com.iquanwai.confucius.biz.po.ActionLog;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
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

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private FragmentClassMemberDao fragmentClassMemberDao;
    @Autowired
    private RiseMemberTypeRepo riseMemberTypeRepo;


    private Map<Integer, String> classNameMap = Maps.newHashMap();
    private Map<Integer, String> groupIdMap = Maps.newHashMap();


    @PostConstruct
    public void init() {
        riseMemberTypeRepo.memberTypes().forEach(item -> {
            classNameMap.put(item.getId(), "className_" + item.getId());
            groupIdMap.put(item.getId(), "groupId_" + item.getId());
        });
    }


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
                List<RiseMember> riseMemberList = riseMemberManager.member(profileId);

                if (!riseMemberList.isEmpty()) {
                    properties.put("roleNames", riseMemberList
                            .stream()
                            .map(RiseMember::getMemberTypeId)
                            .map(Object::toString)
                            .distinct()
                            .collect(Collectors.toList()));
                } else {
                    properties.put("roleNames", Lists.newArrayList("0"));
                }

                List<ClassMember> classMembers = fragmentClassMemberDao.loadActiveByProfileId(profileId);
                if (classMembers == null) {
                    classMembers = Lists.newArrayList(fragmentClassMemberDao.loadLatestByProfileId(profileId));
                }
                if (!classMembers.isEmpty()) {
                    classMembers.forEach(item -> {
                        if (item.getClassName() != null) {
                            properties.put(classNameMap.get(item.getMemberTypeId()), item.getClassName());
                        }
                        if (item.getGroupId() != null) {
                            properties.put(groupIdMap.get(item.getMemberTypeId()), item.getGroupId());
                        }
                    });
                }
                properties.put("isAsst", role != null);
                properties.put("riseId", profile.getRiseId());
                logger.info("trace:\nprofielId:{}\neventName:{}\nprops:{}", profileId, eventName, properties);
                sa.track(profile.getRiseId(), true, eventName, properties);
                //  上线前删掉
                if (ConfigUtils.isDevelopment()) {
                    sa.flush();
                }
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

    @Override
    public void profileSet(Integer profileId, String key, Object value) {
        profileSet(() -> profileId, () -> OperationLogService.props().add(key, value));
    }

    @Override
    public void profileSet(Supplier<Integer> supplier, String key, Object value) {
        this.profileSet(supplier, () -> OperationLogService.props().add(key, value));
    }

    @Override
    public void profileSet(Supplier<Integer> supplier, Supplier<Prop> propSupplier) {
        ThreadPool.execute(() -> {
            Integer profileId = supplier.get();
            Profile profile = profileDao.load(Profile.class, profileId);
            Map<String, Object> properties = propSupplier.get().build();
            logger.info("trace:\nprofielId:{}\neventName:{}\nprops:{}", profileId, "profileSet", properties);
            try {
                sa.profileSet(profile.getRiseId(), true, properties);
                if (ConfigUtils.isDevelopment()) {
                    sa.flush();
                }
            } catch (InvalidArgumentException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        });
    }
}
