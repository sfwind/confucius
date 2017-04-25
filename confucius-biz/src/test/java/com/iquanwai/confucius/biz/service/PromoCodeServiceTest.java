package com.iquanwai.confucius.biz.service;

import com.iquanwai.confucius.biz.TestBase;
import com.iquanwai.confucius.biz.domain.course.operational.PromoCodeService;
import com.iquanwai.confucius.biz.po.PromoCode;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Created by justin on 17/2/14.
 */
public class PromoCodeServiceTest extends TestBase{
    @Autowired
    private PromoCodeService promoCodeService;

    @Test
    public void testDiscount(){
        Double discount = promoCodeService.discount("abce");
        Assert.notNull(discount);
    }

    @Test
    public void testGetPromoCode(){
        PromoCode discount = promoCodeService.getPromoCode("o5h6ywlXxHLmoGrLzH9Nt7uyoHbM");
        Assert.notNull(discount);
    }

    @Test
    public void testUsePromoCode(){
        promoCodeService.usePromoCode("o5h6ywsiXYMcLlex2xt7DRAgQX-A", "abce");
    }
}
