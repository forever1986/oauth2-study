package com.demo.lesson02.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        // 所有访问必须认证
        http.authorizeHttpRequests(auth->auth.anyRequest().authenticated());
        // 默认采用oauth2Login登录
        http.oauth2Login(Customizer.withDefaults());
        return http.build();
    }
}
