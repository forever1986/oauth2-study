package com.demo.lesson06.jwt;

import com.demo.lesson06.entity.LoginUserDetails;
import com.demo.lesson06.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Objects;

/**
 * 用于Spring Security集成JWT认证
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 过滤login接口
        if("/login".equals(request.getRequestURI())){
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求头获取token
        String token = request.getHeader("access_token");
        // 检查获取到的token是否为空或空白字符串。(判断给定的字符串是否包含文本)
        if (!StringUtils.hasText(token)) {
            // 如果token为空，则直接放行请求到下一个过滤器，不做进一步处理并结束当前方法，不继续执行下面代码。
            filterChain.doFilter(request, response);
            return;
        }
        // 解析token
        String userAccount;
        try {
            userAccount = JwtUtil.parseJWT(token);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }
        // 临时缓存中 获取 键 对应 数据
        Object object = redisTemplate.opsForValue().get(userAccount);
        LoginUserDetails loginUser = (LoginUserDetails)object;
        if (Objects.isNull(loginUser)) {
            filterChain.doFilter(request, response);
            return;
        }
        // 将用户信息存入 SecurityConText
        // UsernamePasswordAuthenticationToken 存储用户名 密码 权限的集合
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, null);
        // SecurityContextHolder是Spring Security用来存储当前线程安全的认证信息的容器。
        // 将用户名 密码 权限的集合存入SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        // 放行
        filterChain.doFilter(request, response);
    }
}
