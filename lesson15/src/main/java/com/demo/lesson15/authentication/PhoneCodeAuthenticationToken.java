package com.demo.lesson15.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Collections;
import java.util.Set;

/**
 * 自定义AuthenticationToken
 */
public class PhoneCodeAuthenticationToken extends AbstractAuthenticationToken {

    // 手机验证码模式
    private final AuthorizationGrantType authorizationGrantType;

    // 客户端信息
    private final Authentication clientPrincipal;

    // 手机号码
    private final String phoneNum;

    // 验证码
    private final Object credentials;

    // 授权范围
    private final Set<String> scopes;

    public PhoneCodeAuthenticationToken(AuthorizationGrantType authorizationGrantType, Authentication clientPrincipal, Object credentials, String phoneNum, Set<String> scopes) {
        super(Collections.emptyList());
        this.authorizationGrantType = authorizationGrantType;
        this.clientPrincipal = clientPrincipal;
        this.credentials = credentials;
        this.phoneNum = phoneNum;
        this.scopes = scopes;
    }

    public AuthorizationGrantType getAuthorizationGrantType() {
        return authorizationGrantType;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.clientPrincipal;
    }
}
