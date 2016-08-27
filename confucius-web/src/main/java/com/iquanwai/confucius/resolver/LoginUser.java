package com.iquanwai.confucius.resolver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by tomas on 3/17/16.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {
    private String openId;
    private String weixinName;

    public static LoginUser defaultUser(){
        return new LoginUser("oK881wQekezGpw6rq790y_vAY_YY","风之伤");
    }
}
