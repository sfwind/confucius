package com.iquanwai.confucius.web.operation;

import com.iquanwai.confucius.biz.util.CommonUtils;
import lombok.Data;

/**
 * Created by justin on 17/2/20.
 */
@Data
public class PromoCodeDto {
    private int	id;
    private String code;
    private String name;
    private String avatar;
    private String url;

    public static void main(String[] args) {
        for(int i=1;i<10;i++) {
            String h = "O";
            while(h.contains("O")|| h.contains("0") ||
                    h.contains("I") || h.contains("1")){
                h = CommonUtils.randomString(4).toUpperCase();
            }
            System.out.println(h);
        }
    }
}
