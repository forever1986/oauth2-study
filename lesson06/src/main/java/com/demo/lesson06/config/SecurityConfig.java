package com.demo.lesson06.config;

import com.alibaba.fastjson.JSON;
import com.demo.lesson06.jwt.JwtAuthenticationTokenFilter;
import com.demo.lesson06.result.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import java.io.IOException;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean
    public AuthenticationManager authenticationManager(){
        // 配置合适的AuthenticationProvider
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        // 为AuthenticationProvider设置UserDetailsService
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        // 创建AuthenticationManager
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth->auth
                        //允许/login访问
                        .requestMatchers("/login").permitAll().anyRequest().authenticated())
                // 禁用csrf，因为有post请求
                .csrf(AbstractHttpConfigurer::disable)
                // 添加到顾虑去链路中，确保在AuthorizationFilter过滤器之前
                .addFilterBefore(jwtAuthenticationTokenFilter, AuthorizationFilter.class)
                // 由于采用token方式认证，因此可以关闭session管理
                .sessionManagement(SessionManagementConfigurer::disable)
                // 禁用原来登录页面
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用系统原有的登出
                .logout(LogoutConfigurer::disable)
                // 异常处理
                .exceptionHandling(exception -> exception.authenticationEntryPoint(new AuthenticationEntryPoint(){
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        authException.printStackTrace();
                        Result<String> result = Result.failed("认证失败请重新登录");
                        String json = JSON.toJSONString(result);
                        response.setContentType("application/json;charset=utf-8");
                        response.getWriter().println(json);
                    }
                }));
        return http.build();
    }
}
