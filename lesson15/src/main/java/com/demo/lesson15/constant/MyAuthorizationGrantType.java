package com.demo.lesson15.constant;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

public class MyAuthorizationGrantType {

    // 手机验证码模式
    public static final AuthorizationGrantType PHONE_CODE = new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:phone_code");
}
