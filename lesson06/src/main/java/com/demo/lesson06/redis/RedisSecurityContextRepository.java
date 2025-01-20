package com.demo.lesson06.redis;

import com.demo.lesson06.entity.LoginUserDetails;
import com.demo.lesson06.redis.context.MySupplierDeferredSecurityContext;
import com.demo.lesson06.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.DeferredSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 授权服务器读取存储在Redis的用户信息，可以做为用户认证
 */
public class RedisSecurityContextRepository implements SecurityContextRepository {


    private RedisTemplate redisTemplate;

    public RedisSecurityContextRepository(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        HttpServletRequest request = requestResponseHolder.getRequest();
        return readSecurityContextFromRedis(request);
    }

    @Override
    public DeferredSecurityContext loadDeferredContext(HttpServletRequest request) {
        Supplier<SecurityContext> supplier = () -> readSecurityContextFromRedis(request);
        return new MySupplierDeferredSecurityContext(supplier, SecurityContextHolder.getContextHolderStrategy());
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        return false;
    }

    private SecurityContext readSecurityContextFromRedis(HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("access_token");
        // 检查获取到的token是否为空或空白字符串。(判断给定的字符串是否包含文本)
        if (!StringUtils.hasText(token)) {
            // 如果token为空，则直接放行请求到下一个过滤器，不做进一步处理并结束当前方法，不继续执行下面代码。
            return null;
        }
        // 解析token
        String userAccount;
        try {
            userAccount = JwtUtil.parseJWT(token);
        } catch (Exception e) {
            throw new AccessDeniedException("token格式有误");
        }
        // 临时缓存中 获取 键 对应 数据
        Object object = redisTemplate.opsForValue().get(userAccount);
        LoginUserDetails loginUser = (LoginUserDetails)object;
        if (Objects.isNull(loginUser)) {
            throw new AccessDeniedException("用户未登录");
        }
        SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
        SecurityContext securityContext = securityContextHolderStrategy.createEmptyContext();
        // 将用户信息存入 SecurityConText
        // UsernamePasswordAuthenticationToken 存储用户名 密码 权限的集合
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, null);
        // SecurityContextHolder是Spring Security用来存储当前线程安全的认证信息的容器。
        // 将用户名 密码 权限的集合存入SecurityContextHolder
        securityContext.setAuthentication(authenticationToken);
        return securityContext;
    }
}
