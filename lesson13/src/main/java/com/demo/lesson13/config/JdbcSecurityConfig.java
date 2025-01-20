package com.demo.lesson13.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.util.UUID;

@Configuration
public class JdbcSecurityConfig {

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                // 客户端id
                .clientId("oidc-client")
                // 客户端密码
                .clientSecret("{noop}secret")
                // 客户端认证方式
                .clientAuthenticationMethods(methods ->{
                    methods.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
                })
                // 配置授权码模式
                .authorizationGrantTypes(grantTypes -> {
                    grantTypes.add(AuthorizationGrantType.AUTHORIZATION_CODE);
                })
                // 需要授权确认
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                // 回调地址
                .redirectUri("http://localhost:8080/login/oauth2/code/oidc-client")
                .postLogoutRedirectUri("http://localhost:8080/")
                // 授权范围
                .scopes(scopes->{
                    scopes.add(OidcScopes.OPENID);
                    scopes.add(OidcScopes.PROFILE);
                    scopes.add(OidcScopes.EMAIL);
                })
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    @Bean
    public OAuth2AuthorizationService oAuth2AuthorizationService(){
        return new InMemoryOAuth2AuthorizationService();
    }
}
