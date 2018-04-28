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
import com.iquanwai.confucius.biz.dao.quanwai.EmployeeDao;
import com.iquanwai.confucius.biz.domain.course.signup.MemberTypeManager;
import com.iquanwai.confucius.biz.domain.course.signup.RiseMemberManager;
import com.iquanwai.confucius.biz.domain.fragmentation.ClassMember;
import com.iquanwai.confucius.biz.po.ActionLog;
import com.iquanwai.confucius.biz.po.OperationLog;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.Role;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.po.fragmentation.RiseClassMember;
import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;
import com.iquanwai.confucius.biz.po.quanwai.QuanwaiEmployee;
import com.iquanwai.confucius.biz.util.ConfigUtils;
import com.iquanwai.confucius.biz.util.ThreadPool;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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
    private UserRoleDao userRoleDao;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private FragmentClassMemberDao fragmentClassMemberDao;
    @Autowired
    private MemberTypeManager memberTypeManager;
    @Autowired
    private EmployeeDao employeeDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;


    private Map<Integer, String> classNameMap = Maps.newHashMap();
    private Map<Integer, String> groupIdMap = Maps.newHashMap();


    @PostConstruct
    public void init() {
        memberTypeManager.memberTypes().forEach(item -> {
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
                if (CollectionUtils.isEmpty(classMembers)) {
                    ClassMember classMember = fragmentClassMemberDao.loadLatestByProfileId(profileId);
                    if (classMember != null) {
                        classMembers.add(classMember);
                    }
                }
                classMembers.forEach(item -> {
                    if (item.getClassName() != null) {
                        properties.put(classNameMap.get(item.getMemberTypeId()), item.getClassName());
                    }
                    if (item.getGroupId() != null) {
                        properties.put(groupIdMap.get(item.getMemberTypeId()), item.getGroupId());
                    }
                });
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

    @Override
    public void refreshProfiles(List<Integer> profileIds) {
        // 员工
        Map<Integer, List<QuanwaiEmployee>> employeeRepo = employeeDao.loadAll(QuanwaiEmployee.class).stream().filter(item -> item.getProfileId() != null).collect(Collectors.groupingBy(QuanwaiEmployee::getProfileId));
        // vip
        Map<Integer, List<UserRole>> roleRepo = userRoleDao.loadAll(UserRole.class).stream().filter(item -> item.getProfileId() != null).filter(item -> !item.getDel()).collect(Collectors.groupingBy(UserRole::getProfileId));
        // 会员
        Map<Integer, List<RiseMember>> riseMemberRepo = riseMemberDao.loadByProfileIds(profileIds).stream().filter(item -> item.getProfileId() != null).filter(item -> !item.getMemberTypeId().equals(RiseMember.COURSE)).collect(Collectors.groupingBy(RiseMember::getProfileId));
        // 身份信息
        Map<Integer, List<Profile>> profileRepo = profileDao.loadByProfileIds(profileIds).stream().collect(Collectors.groupingBy(Profile::getId));

        profileIds.forEach(profileId -> ThreadPool.execute(() -> {
            try {
                if (profileRepo.containsKey(profileId)) {
                    profileRepo.get(profileId).stream().findFirst().ifPresent(profile -> {
                        Prop prop = OperationLogService.props();
                        // roleNames,openid,classname,gropuid,isAsst,isEmplyee
                        List<RiseMember> members = riseMemberRepo.get(profileId);

                        if (!CollectionUtils.isEmpty(members)) {
                            prop.add("roleNames", members.stream().map(RiseMember::getMemberTypeId).map(String::valueOf).collect(Collectors.toList()));
                        }
                        prop.add("openId", profile.getOpenid());
                        List<ClassMember> classMembers = fragmentClassMemberDao.loadByProfileId(profileId);
                        if (!CollectionUtils.isEmpty(classMembers)) {
                            classMembers.stream().collect(Collectors.groupingBy(ClassMember::getMemberTypeId)).forEach((memberId, list) -> {
                                list.stream().sorted(((o1, o2) -> o2.getId() - o1.getId())).findFirst().ifPresent(item -> {
                                    if (item.getClassName() != null) {
                                        prop.add("className_" + item.getMemberTypeId(), item.getClassName());
                                    }
                                    if (item.getGroupId() != null) {
                                        prop.add("groupId_" + item.getMemberTypeId(), item.getGroupId());
                                    }
                                });
                            });
                        } else {
                            RiseClassMember riseClassMember = riseClassMemberDao.loadLatestRiseClassMember(profileId);
                            if (riseClassMember != null) {
                                if (riseClassMember.getClassName() != null) {
                                    Integer memberTypeId;
                                    if (Integer.parseInt(riseClassMember.getClassName()) % 2 == 0) {
                                        memberTypeId = 5;
                                    } else {
                                        memberTypeId = 3;
                                    }
                                    prop.add("className_" + memberTypeId, riseClassMember.getClassName());
                                    if (riseClassMember.getGroupId() != null) {
                                        prop.add("groupId_" + memberTypeId, riseClassMember.getGroupId());
                                    }
                                }
                            }
                        }
                        if (roleRepo.containsKey(profileId)) {
                            roleRepo.get(profileId).stream().filter(item -> Role.isAsst(item.getRoleId())).findFirst().ifPresent(item -> prop.add("isAsst", true));
                        }
                        if (employeeRepo.containsKey(profileId)) {
                            employeeRepo.get(profileId).stream().findFirst().ifPresent(item -> prop.add("employee", true));
                        }
                        Map<String, Object> properties = prop.build();
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
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }));
    }
}
