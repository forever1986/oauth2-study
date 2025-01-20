package com.demo.lesson04.config;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

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
                .oidc(withDefaults())
        ;
        // 资源服务器默认jwt配置
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


//    @Bean
//    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate){
//        return new JdbcRegisteredClientRepository(jdbcTemplate);
//    }
//
//    @Bean
//    public OAuth2AuthorizationService oAuth2AuthorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository){
//        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
//    }
//
//    @Bean
//    public OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository){
//        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
//    }
}
