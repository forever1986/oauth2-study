package com.demo.lesson09.config;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
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
                .requestMatchers("/jwt").permitAll()
                .anyRequest().authenticated()).formLogin(withDefaults());
        return http.build();
    }

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
                    methods.add(ClientAuthenticationMethod.CLIENT_SECRET_POST);
                    methods.add(ClientAuthenticationMethod.CLIENT_SECRET_JWT);
                    methods.add(ClientAuthenticationMethod.PRIVATE_KEY_JWT);
                })
                // 客户端配置，使用RS256加密，公钥地址：http://localhost:9000/jwt
                .clientSettings(ClientSettings.builder()
                        .tokenEndpointAuthenticationSigningAlgorithm(SignatureAlgorithm.RS256)
                        .jwkSetUrl("http://localhost:9000/jwt")
                        .build())
                // 这里为了方便采用客户端模式，而非授权码模式
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                // 回调地址
                .redirectUri("http://localhost:8080/login/oauth2/code/oidc-client")
                .postLogoutRedirectUri("http://localhost:8080/")
                // 授权范围
                .scopes(scopes->{
                    scopes.add(OidcScopes.OPENID);
                    scopes.add(OidcScopes.PROFILE);
                })
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

}
