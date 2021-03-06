package com.iquanwai.confucius.biz.po.common.customer;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/2/8.
 */
@Data
public class Profile {
    private int id;
    private String openid;    //用户的标识，对当前公众号唯一
    private String nickname; //用户的昵称
    private String city;    //用户所在城市
    private String country;    //用户所在国家
    private String province; //	用户所在省份
    private String headimgurl;    //用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像），用户没有头像时该项为空。若用户更换头像，原有头像URL将失效。
    private Date headImgUrlCheckTime; // 头像最近校验时间
    private String mobileNo;  //手机号
    private String email;  //邮箱
    private String industry; //行业
    private String function; //职业
    private String workingLife; //工作年限
    private String workYear;//参加工作时间
    private String realName; //真名
    private String signature; //签名
    private Integer point; //总积分
    private Integer isFull; //资料是否填写完毕
    private String riseId; //riseid
    private String unionid;    //只有在用户将公众号绑定到微信开放平台帐号后，才会出现该字段。详见：获取用户个人信息（UnionID机制）
    private String address;//地址
    private String married; //婚恋情况
    private String receiver; //收件人
    private String weixinId; //微信id
    private String memberId; //学号

    private Integer riseMember; //0-免费用户,1-会员,2-课程单买用户
    private Integer role;//非db字段 用户角色

    //默认头像
    public static final String DEFAULT_AVATAR = "https://static.iqycamp.com/images/default_avatar.png";

    public enum ProfileType {
        PC(1), MOBILE(2), MINI(3);
        private int value;

        ProfileType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
