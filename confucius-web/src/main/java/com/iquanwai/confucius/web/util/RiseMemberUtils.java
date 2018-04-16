package com.iquanwai.confucius.web.util;

import com.iquanwai.confucius.biz.po.fragmentation.RiseMember;

public class RiseMemberUtils {

    /**
     * memberType=>member
     *
     * @param type
     * @return
     */
    public static String convert(Integer type) {
        switch (type) {
            case RiseMember.HALF:
                return "专业版半年";
            case RiseMember.ANNUAL:
                return "专业版一年";
            case RiseMember.ELITE:
                return "商学院会员";
            case RiseMember.HALF_ELITE:
                return "精英版半年";
            case RiseMember.CAMP:
                return "专项课用户";
            case RiseMember.COURSE:
                return "单买课程";
            case RiseMember.BS_APPLICATION:
                return "商学院申请";
            case RiseMember.BUSINESS_THOUGHT:
                return "商业思维会员";

        }
        return "未知";
    }
}
