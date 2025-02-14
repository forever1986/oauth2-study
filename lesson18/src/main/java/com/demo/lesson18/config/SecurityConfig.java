package com.demo.lesson18.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
        http
                // 所有访问都必须认证
                .authorizeExchange(exchange -> exchange.anyExchange().authenticated())
                // 禁用csrf，因为登录和登出是post请求，csrf会屏蔽掉post请求
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // 默认配置
                .formLogin(Customizer.withDefaults());
        return http.build();
    }

    /**
     * 默认一个用户
     */
    @Bean
    public ReactiveUserDetailsService userDetailsService(){
        return new ReactiveUserDetailsService(){
            @Override
            public Mono<UserDetails> findByUsername(String username) {
                UserDetails userDetails = User.withUsername("test")
                        .password("{noop}1234")
                        .build();
                return Mono.just(userDetails);
            }
        };
    }

}
