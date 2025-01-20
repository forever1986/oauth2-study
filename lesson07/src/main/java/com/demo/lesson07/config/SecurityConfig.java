package com.demo.lesson07.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // userInfo接口需要SCOPE_profile权限
                        .requestMatchers("/userInfo").hasAuthority("SCOPE_profile")
                        // 其它访问都需要鉴权
                        .anyRequest().authenticated()
                )
                // 资源服务器默认配置
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

}
