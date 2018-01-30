package com.iquanwai.confucius.biz.domain.asst;

import com.google.common.collect.Lists;
import com.iquanwai.confucius.biz.dao.asst.AsstUpDefaultDao;
import com.iquanwai.confucius.biz.dao.asst.AsstUpExecutionDao;
import com.iquanwai.confucius.biz.dao.asst.AsstUpStandardDao;
import com.iquanwai.confucius.biz.dao.common.customer.ProfileDao;
import com.iquanwai.confucius.biz.dao.common.permission.UserRoleDao;
import com.iquanwai.confucius.biz.po.asst.AsstUpDefault;
import com.iquanwai.confucius.biz.po.asst.AsstUpExecution;
import com.iquanwai.confucius.biz.po.asst.AsstUpStandard;
import com.iquanwai.confucius.biz.po.common.customer.Profile;
import com.iquanwai.confucius.biz.po.common.permisson.UserRole;
import com.iquanwai.confucius.biz.util.page.Page;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AsstUpServiceImpl implements AsstUpService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AsstUpDefaultDao asstUpDefaultDao;
    @Autowired
    private AsstUpStandardDao asstUpStandardDao;
    @Autowired
    private AsstUpExecutionDao asstUpExecutionDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private ProfileDao profileDao;


    @Override
    public AsstUpExecution loadUpGradeExecution(Integer profileId) {
        return asstUpExecutionDao.queryByProfileId(profileId);
    }

    @Override
    public Integer updateExecution(AsstUpExecution asstUpExecution) {
        return asstUpExecutionDao.update(asstUpExecution);
    }

    /**
     * 根据excel根据完成表
     *
     * @param file
     * @return
     */
    @Override
    public void updateExecution(MultipartFile file) {
        List<AsstUpExecution> asstUpExecutions = genAsstUpExecution(file);
        asstUpExecutions.forEach(asstUpExecution -> {
            Integer profileId = asstUpExecution.getProfileId();
            AsstUpExecution existExecution = asstUpExecutionDao.queryByProfileId(profileId);
            //增量
            asstUpExecution.setId(existExecution.getId());
            if(asstUpExecution.getReviewNumber()==null){
                asstUpExecution.setReviewNumber(existExecution.getReviewNumber());
            }else {
                asstUpExecution.setReviewNumber(existExecution.getReviewNumber()+asstUpExecution.getReviewNumber());
            }
            if(asstUpExecution.getValidReviewNumber()==null){
                asstUpExecution.setValidReviewNumber(existExecution.getValidReviewNumber());
            }else{
                asstUpExecution.setValidReviewNumber(existExecution.getValidReviewNumber()+asstUpExecution.getValidReviewNumber());
            }
            if(asstUpExecution.getRequestReviewNumber()==null){
                asstUpExecution.setRequestReviewNumber(existExecution.getRequestReviewNumber());
            }
            else{
                asstUpExecution.setRequestReviewNumber(existExecution.getRequestReviewNumber()+asstUpExecution.getRequestReviewNumber());
            }
            if(asstUpExecution.getHighQualityAnswer()==null){
                asstUpExecution.setHighQualityAnswer(existExecution.getHighQualityAnswer());
            }else {
                asstUpExecution.setHighQualityAnswer(existExecution.getHighQualityAnswer()+asstUpExecution.getHighQualityAnswer());
            }
            //覆盖
            if(asstUpExecution.getUpGrade()==null){
                asstUpExecution.setUpGrade(existExecution.getUpGrade());
            }
            if(asstUpExecution.getHostNumber()==null){
                asstUpExecution.setHostNumber(existExecution.getHostNumber());
            }
            if(asstUpExecution.getHostScore()==null){
                asstUpExecution.setHostScore(existExecution.getHostScore());
            }
            if(asstUpExecution.getMainPointNumber()==null){
                asstUpExecution.setMainPointNumber(existExecution.getMainPointNumber());
            }
            if(asstUpExecution.getMainPointScore()==null){
                asstUpExecution.setMainPointScore(existExecution.getMainPointScore());
            }
            if(asstUpExecution.getOnlineAnswer()==null){
                asstUpExecution.setOnlineAnswer(existExecution.getOnlineAnswer());
            }
            if(asstUpExecution.getSwing()==null){
                asstUpExecution.setSwing(existExecution.getSwing());
            }
            if(asstUpExecution.getOnlineOrSwingNumber() == null){
                asstUpExecution.setOnlineOrSwingNumber(existExecution.getOnlineOrSwingNumber());
            }
            if(asstUpExecution.getOnlineScore() ==null){
                asstUpExecution.setOnlineScore(existExecution.getOnlineScore());
            }
            if(asstUpExecution.getCampNumber()== null){
                asstUpExecution.setCampNumber(existExecution.getCampNumber());
            }
            if(asstUpExecution.getAsstNumber()==null){
                asstUpExecution.setAsstNumber(existExecution.getAsstNumber());
            }
            if(asstUpExecution.getCampScore()==null){
                asstUpExecution.setCampScore(existExecution.getCampScore());
            }
            if(asstUpExecution.getMonthlyWork()==null){
                asstUpExecution.setMonthlyWork(existExecution.getMonthlyWork());
            }
            if(asstUpExecution.getFosterNew()==null){
                asstUpExecution.setFosterNew(existExecution.getFosterNew());
            }
            if(asstUpExecution.getCompanyTrainNumber()==null){
                asstUpExecution.setCompanyTrainNumber(existExecution.getCompanyTrainNumber());
            }
            if(asstUpExecution.getCompanyTrainScore()==null){
                asstUpExecution.setCompanyTrainScore(existExecution.getCompanyTrainScore());
            }
            asstUpExecutionDao.update(asstUpExecution);
        });
    }

    @Override
    public AsstUpExecution load(Integer id) {
        return asstUpExecutionDao.load(AsstUpExecution.class, id);
    }

    @Override
    public AsstUpStandard loadStandard(Integer profileId) {
        return asstUpStandardDao.queryByProfileId(profileId);
    }

    @Override
    public Integer updateStandard(AsstUpStandard asstUpStandard) {
        return asstUpStandardDao.update(asstUpStandard);
    }

    @Override
    public List<UserRole> loadAssists(Page page) {
        List<UserRole> userRoles = userRoleDao.loadAssistsList(page);
        page.setTotal(userRoleDao.loadAssistsCount());
        return userRoles;
    }

    @Override
    public List<AsstUpDefault> loadAssistDefault() {
        List<AsstUpDefault> asstUpDefaults = asstUpDefaultDao.loadAll(AsstUpDefault.class);
        return asstUpDefaults.stream().filter(asstUpDefault -> asstUpDefault.getDel() == 0).collect(Collectors.toList());
    }

    @Override
    public AsstUpDefault loadDefaultByRoleId(Integer roleId) {
        return asstUpDefaultDao.queryByRoleId(roleId);
    }

    @Override
    public Integer insertStandard(AsstUpStandard asstUpStandard) {
        return asstUpStandardDao.insert(asstUpStandard);
    }

    @Override
    public Integer insertExecution(Integer standardId, Integer profileId, Integer roleId, Date startDate) {
        return asstUpExecutionDao.insert(standardId, profileId, roleId, startDate);
    }


    private List<AsstUpExecution> genAsstUpExecution(MultipartFile file) {
        List<AsstUpExecution> asstUpExecutions = Lists.newArrayList();
        Workbook workbook = null;
        try {
            workbook = Workbook.getWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheet(0);

            for (int row = 2; row < sheet.getRows(); row++) {
                AsstUpExecution asstUpExecution = new AsstUpExecution();

                String riseId = sheet.getCell(2, row).getContents();
                Profile profile = profileDao.queryByRiseId(riseId);

                asstUpExecution.setProfileId(profile.getId());
                String reviewNumber = sheet.getCell(8, row).getContents();
                if (!"".equals(reviewNumber)) {
                    asstUpExecution.setReviewNumber(Integer.parseInt(reviewNumber));
                }
                String validReviewNumber = sheet.getCell(9, row).getContents();
                if (!"".equals(validReviewNumber)) {
                    asstUpExecution.setValidReviewNumber(Integer.parseInt(validReviewNumber));
                }
                String requestReviewNumber = sheet.getCell(11, row).getContents();
                if (!"".equals(requestReviewNumber)) {
                    asstUpExecution.setRequestReviewNumber(Integer.parseInt(requestReviewNumber));
                }
                String highQualityAnswer = sheet.getCell(12, row).getContents();
                if (!"".equals(highQualityAnswer)) {
                    asstUpExecution.setHighQualityAnswer(Integer.parseInt(highQualityAnswer));
                }
                String upgrade = sheet.getCell(13, row).getContents();
                if (!"".equals(upgrade)) {
                    asstUpExecution.setUpGrade(upgrade);
                }
                String hostNumber = sheet.getCell(14, row).getContents();
                if (!"".equals(hostNumber)) {
                    asstUpExecution.setHostNumber(Integer.parseInt(hostNumber));
                }
                String hostScore = sheet.getCell(15, row).getContents();
                if (!"".equals(hostScore)) {
                    asstUpExecution.setHostScore(new BigDecimal(hostScore).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
                String mainPointNumber = sheet.getCell(16, row).getContents();
                if (!"".equals(mainPointNumber)) {
                    asstUpExecution.setMainPointNumber(Integer.parseInt(mainPointNumber));
                }
                String mainPointScore = sheet.getCell(17, row).getContents();
                if (!"".equals(mainPointScore)) {
                    asstUpExecution.setMainPointScore(new BigDecimal(mainPointScore).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
                String onlineAnswer = sheet.getCell(18, row).getContents();
                if (!"".equals(onlineAnswer)) {
                    asstUpExecution.setOnlineAnswer(onlineAnswer);
                }
                String swing = sheet.getCell(19, row).getContents();
                if (!"".equals(swing)) {
                    asstUpExecution.setSwing(swing);
                }
                String onlineOrSwingNumber = sheet.getCell(20, row).getContents();
                if (!"".equals(onlineOrSwingNumber)) {
                    asstUpExecution.setOnlineOrSwingNumber(Integer.parseInt(onlineOrSwingNumber));
                }
                String onlineScore = sheet.getCell(21, row).getContents();
                if (!"".equals(onlineScore)) {
                    asstUpExecution.setOnlineScore(new BigDecimal(onlineScore).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
                String campNumber = sheet.getCell(22, row).getContents();
                if (!"".equals(campNumber)) {
                    asstUpExecution.setCampNumber(Integer.parseInt(campNumber));
                }
                String asstNumber = sheet.getCell(23,row).getContents();
                if(!"".equals(asstNumber)){
                    asstUpExecution.setAsstNumber(Integer.parseInt(asstNumber));
                }
                String campScore = sheet.getCell(24,row).getContents();
                if(!"".equals(campScore)){
                   asstUpExecution.setCampScore(new BigDecimal(campScore).setScale(2,BigDecimal.ROUND_HALF_UP));
                }
                String monthlyWork = sheet.getCell(25,row).getContents();
                if(!"".equals(monthlyWork)){
                    asstUpExecution.setMonthlyWork(monthlyWork);
                }
                String fosterNew = sheet.getCell(27,row).getContents();
                if(!"".equals(fosterNew)){
                    asstUpExecution.setFosterNew(Integer.parseInt(fosterNew));
                }
                String companyTrainNumber = sheet.getCell(28,row).getContents();
                if(!"".equals(companyTrainNumber)){
                    asstUpExecution.setCompanyTrainNumber(Integer.parseInt(companyTrainNumber));
                }
                String companyTrainScore= sheet.getCell(29,row).getContents();
                if(!"".equals(companyTrainScore)){
                    asstUpExecution.setCompanyTrainScore(new BigDecimal(companyTrainScore).setScale(2,BigDecimal.ROUND_HALF_UP));
                }
                asstUpExecutions.add(asstUpExecution);
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
        return asstUpExecutions;
    }
}
