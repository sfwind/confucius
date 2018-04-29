package com.iquanwai.confucius.biz.po.fragmentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nethunder on 2017/4/6.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberType {
    private Integer id; // MemberId
    private Double fee; // 会员费用
    private Double initPrice; //原价
    private String name; // 会员名
    private String description; // 描述
    private Integer openMonth; // 会员时长
    private String startTime; // 开启时间 非DB字段
    private String endTime; // 结束时间 非DB字段
    private Boolean del; // 是否删除
    private Boolean purchaseSwitch; //购买开关

    public MemberType copy(){
        MemberType temp = new MemberType();
        temp.setId(this.id);
        temp.setFee(this.fee);
        temp.setName(this.name);
        temp.setDescription(this.description);
        temp.setOpenMonth(this.openMonth);
        temp.setStartTime(this.startTime);
        temp.setEndTime(this.endTime);
        temp.setDel(this.del);
        temp.setInitPrice(this.initPrice);
        temp.setPurchaseSwitch(this.purchaseSwitch);
        return temp;
    }
}
