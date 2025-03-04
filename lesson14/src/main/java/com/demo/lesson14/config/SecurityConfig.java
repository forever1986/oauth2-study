package com.demo.lesson14.config;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.util.UUID;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    // 自定义授权服务器的Filter链
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                // oidc配置
                .oidc(withDefaults());
        // 同时作为资源服务器，使用/userinfo接口
        http.oauth2ResourceServer((resourceServer) -> resourceServer.jwt(withDefaults()));
        // 异常处理
        http.exceptionHandling((exceptions) -> exceptions.authenticationEntryPoint(
                new LoginUrlAuthenticationEntryPoint("/login")));
        return http.build();
    }

    // 自定义Spring Security的链路。如果自定义授权服务器的Filter链，则原先自动化配置将会失效，因此也要配置Spring Security
    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authorize) -> authorize
                .anyRequest().authenticated()).formLogin(withDefaults());
        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {

        // 客户端访问API A的授权
        RegisteredClient registeredClient1 = RegisteredClient.withId(UUID.randomUUID().toString())
                // 客户端id
                .clientId("api-a")
                // 客户端密码
                .clientSecret("{noop}secret1")
                // 客户端认证方式：因为是API A做令牌交换，因此没有用户参与，使用客户端模式
                .clientAuthenticationMethods(methods ->{
                    methods.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
                })
                // 配置令牌交换
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
                })
                .build();

        // API A访问API B的授权
        RegisteredClient registeredClient2 = RegisteredClient.withId(UUID.randomUUID().toString())
                // 客户端id
                .clientId("api-b")
                // 客户端密码
                .clientSecret("{noop}secret2")
                // 客户端认证方式
                .clientAuthenticationMethods(methods ->{
                    methods.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
                })
                // 配置授权码模式
                .authorizationGrantTypes(grantTypes -> {
                    grantTypes.add(AuthorizationGrantType.TOKEN_EXCHANGE);
                })
                // 回调地址
                .redirectUri("http://localhost:8080/login/oauth2/code/oidc-client")
                .postLogoutRedirectUri("http://localhost:8080/")
                // 授权范围
                .scopes(scopes->{
                    scopes.add("clientB");
                })
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient1, registeredClient2);
    }

}
